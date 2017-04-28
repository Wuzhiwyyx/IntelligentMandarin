package com.example.wuzhi.intelligentmandarin;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wuzhi.intelligentmandarin.DataClass.Dialog;
import com.example.wuzhi.intelligentmandarin.DataClass.DialogAdapter;
import com.example.wuzhi.intelligentmandarin.VoiceTools.Result;
import com.example.wuzhi.intelligentmandarin.VoiceTools.XmlResultParser;
import com.google.gson.Gson;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DialogueActivity extends AppCompatActivity {

    private TextView score, details;
    private RecyclerView dialog;
    private SwipeRefreshLayout swipeRefresh;
    private Button micro;
    private DialogAdapter adapter;
    private Toast mToast;
    private List<String> dataSet = new ArrayList<>();
    private List<Double> scoreList = new ArrayList<>();
    private int position = 0;

    private String voicer = "xiaoyuan";
    private static SpeechSynthesizer mTts;
    private static SynthesizerListener mTtsListener;
    private SpeechEvaluator mIse;
    private EvaluatorListener mEvaluatorListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue);
        initView();
        initFunction();
        mTts.startSpeaking(dataSet.get(position), mTtsListener);
        position++;
    }


    public void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("情景对话");
        setSupportActionBar(toolbar);

        score = (TextView) findViewById(R.id.score);
        details = (TextView) findViewById(R.id.details);
        details.setMovementMethod(new ScrollingMovementMethod());

        micro = (Button) findViewById(R.id.micro);
        micro.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        micro.setBackgroundResource(R.drawable.micro_pressed);
                        Log.i("TAG", "------->down");
                        if (position < dataSet.size())
                            mIse.startEvaluating(dataSet.get(position), null, mEvaluatorListener);
                        return true;
                    case MotionEvent.ACTION_UP:
                        micro.setBackgroundResource(R.drawable.micro);
                        Log.i("TAG", "------->up");
                        mIse.stopEvaluating();
                        position++;
                        if (position < dataSet.size()) {
                            mTts.startSpeaking(dataSet.get(position), mTtsListener);
                            position++;
                            if (position < dataSet.size())
                                dialog.smoothScrollToPosition(position);
                        }
                        return true;
                }
                return false;
            }
        });
        setEvaText();
        dialog = (RecyclerView) findViewById(R.id.dialog);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        dialog.setLayoutManager(layoutManager);
        adapter = new DialogAdapter(dataSet);
        dialog.setAdapter(adapter);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDialog();

            }
        });

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
    }

    public void initFunction() {
        mTts = SpeechSynthesizer.createSynthesizer(DialogueActivity.this, new InitListener() {
            @Override
            public void onInit(int i) {
                if (i != ErrorCode.SUCCESS) {
                    Toast.makeText(DialogueActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mTtsListener = new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {
                dialog.smoothScrollToPosition(position);
            }

            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {
                mToast.setText("缓冲进度：" + i + "%");
                mToast.show();
            }

            @Override
            public void onSpeakPaused() {

            }

            @Override
            public void onSpeakResumed() {

            }

            @Override
            public void onSpeakProgress(int i, int i1, int i2) {

            }

            @Override
            public void onCompleted(SpeechError speechError) {

            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        };
        setSpeechParam();

        mIse = SpeechEvaluator.createEvaluator(DialogueActivity.this, null);
        mEvaluatorListener = new EvaluatorListener() {
            @Override
            public void onVolumeChanged(int i, byte[] bytes) {
                mToast.setText("Volume" + i);
                mToast.show();
            }

            @Override
            public void onBeginOfSpeech() {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onResult(EvaluatorResult evaluatorResult, boolean b) {
                if (b) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(evaluatorResult.getResultString());
                    if (!TextUtils.isEmpty(builder)) {
                        XmlResultParser resultParser = new XmlResultParser();
                        Result result = resultParser.parse(builder.toString());
                        String resultString = result.toString();
                        details.setText(details.getText() + getDetails(resultString));
                        scoreList.add(getScore(resultString));
                        Double sum = 0.0;
                        for (Double d:scoreList) {
                            sum += d;
                        }
                        DecimalFormat df = new DecimalFormat( "0.000");
                        score.setText(df.format(sum / scoreList.size()));
                    }
//                    Toast.makeText(DialogueActivity.this, "Evaluate end", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                if (speechError != null) {
                    Toast.makeText(DialogueActivity.this, "Error:" + speechError.getErrorDescription(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        };
        setEvaluateParams();
    }





    private void setSpeechParam() {
        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        mTts.setParameter(SpeechConstant.SPEED, "50");
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }

    private void setEvaluateParams() {
        mIse.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIse.setParameter(SpeechConstant.ISE_CATEGORY, "read_sentence");
        mIse.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mIse.setParameter(SpeechConstant.RESULT_LEVEL, "complete");
    }


    private void setEvaText() {
        dataSet.add("你好");
        dataSet.add("你好");
        dataSet.add("很荣幸见到你，我是普通话小助手，帮助您练习普通话");
        dataSet.add("你能实现哪些功能");
        dataSet.add("有很多，单字，单词，短语，都可以");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTts.stopSpeaking();
        mTts.destroy();
    }

    public Double getScore(String result) {
        int pos = result.indexOf("总分：");
        String temp = result.substring(pos + 3, pos + 7);
        return Double.parseDouble(temp.trim());
    }

    public String getDetails(String result) {
        int pos = result.indexOf("[朗读详情]");
        return result.substring(pos + 7);
    }

    public static void speak(String s) {
        mTts.startSpeaking(s, mTtsListener);
    }

    public void refreshDialog() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url("http://45.32.55.34:8080/mandarin/getdialog").build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    Dialog dialog = gson.fromJson(responseData, Dialog.class);
                    final List<String> strings = Arrays.asList(dialog.getDialogContent().split("\\s+"));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dataSet.clear();
                            dataSet.addAll(strings);
                            adapter.notifyDataSetChanged();
                            swipeRefresh.setRefreshing(false);
                            position = 0;
                            mTts.startSpeaking(dataSet.get(position), mTtsListener);
                            position++;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
