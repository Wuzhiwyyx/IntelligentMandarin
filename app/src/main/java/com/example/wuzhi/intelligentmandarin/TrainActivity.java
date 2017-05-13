package com.example.wuzhi.intelligentmandarin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wuzhi.intelligentmandarin.DataClass.ExerciseDataSource;
import com.example.wuzhi.intelligentmandarin.DataClass.LearnedSentence;
import com.example.wuzhi.intelligentmandarin.DataClass.LearnedVocabulary;
import com.example.wuzhi.intelligentmandarin.DataClass.LearnedWord;
import com.example.wuzhi.intelligentmandarin.VoiceTools.Result;
import com.example.wuzhi.intelligentmandarin.VoiceTools.XmlResultParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TrainActivity extends AppCompatActivity implements View.OnTouchListener {


    private List<ExerciseDataSource> dataSources;
    private List<DataSourceFragment> dataSourceFragments = new ArrayList<>();
    private Toast mToast;
    private Button micro;
    private TextView wordCount, sentenceCount;
    private DataSourceFragment currentFragment;

    private SpeechSynthesizer mTts;
    private SynthesizerListener mTtsListener;
    private SpeechEvaluator mIse;
    private EvaluatorListener mEvaluatorListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
        initView();
        initFunction();
    }

    public void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Intent intent = getIntent();
        switch (intent.getIntExtra("trainMethod", -1)) {
            case 0:
                toolbar.setTitle("每日练习");
                dataSources = MainActivity.dailyExerciseDataSources;
                break;
            case 1:
                toolbar.setTitle("字词练习");
                dataSources = MainActivity.wordTrainDataSources;
                break;
            case 2:
                toolbar.setTitle("句段练习");
                dataSources = MainActivity.sentenceDataSources;
                break;
            default:
                finish();
        }
        setSupportActionBar(toolbar);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentFragment = dataSourceFragments.get(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        FragmentManager manager = getSupportFragmentManager();
        for (int i = 0; i < dataSources.size(); i++) {
            dataSourceFragments.add(new DataSourceFragment());
        }
        currentFragment = dataSourceFragments.get(0);
        DataSourceFragmentAdapter dataSourceFragmentAdapter = new DataSourceFragmentAdapter(manager, dataSourceFragments, dataSources);
        viewPager.setAdapter(dataSourceFragmentAdapter);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        micro = (Button) findViewById(R.id.micro);
        micro.setOnTouchListener(this);
        wordCount = (TextView) findViewById(R.id.wordCount);
        sentenceCount = (TextView) findViewById(R.id.sentenceCount);
        updateTrainRecord();
    }

    public void initFunction() {
        mTts = SpeechSynthesizer.createSynthesizer(TrainActivity.this, new InitListener() {
            @Override
            public void onInit(int i) {
                if (i != ErrorCode.SUCCESS) {
                    Toast.makeText(TrainActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mTtsListener = new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {

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

        mIse = SpeechEvaluator.createEvaluator(TrainActivity.this, null);
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
                        currentFragment.setEvaluateResult(resultString);
                    }
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                if (speechError != null) {
                    Toast.makeText(TrainActivity.this, "Error:" + speechError.getErrorDescription(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        };
    }

    private void setSpeechParam() {
        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        mTts.setParameter(SpeechConstant.VOICE_NAME, MainActivity.voice);
        mTts.setParameter(SpeechConstant.SPEED, "50");
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }

    private void setEvaluateParams(String category) {
        mIse.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIse.setParameter(SpeechConstant.ISE_CATEGORY, category);
        mIse.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mIse.setParameter(SpeechConstant.RESULT_LEVEL, "complete");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTts.stopSpeaking();
        mTts.destroy();
    }

    public void startEvaluate(String content, int TYPE) {
        switch (TYPE) {
            case ExerciseDataSource.LEARNED_WORD:
                setEvaluateParams("read_syllable");
                break;
            case ExerciseDataSource.LEARNED_VOCABULARY:
                setEvaluateParams("read_word");
                break;
            case ExerciseDataSource.LEARNED_SENTENCE:
                setEvaluateParams("read_sentence");
                break;
        }
        mIse.startEvaluating(content, null, mEvaluatorListener);
    }


    public void startSpeech(String content) {
        int code = mTts.startSpeaking(content, mTtsListener);
        if (code != ErrorCode.SUCCESS) {
            Toast.makeText(TrainActivity.this, "合成失败，错误码" + code, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                micro.setBackgroundResource(R.drawable.micro_pressed);
                if (currentFragment.getExerciseData().getType() != ExerciseDataSource.LEARNED_SENTENCE) {
                    startEvaluate(currentFragment.getElement().getText().toString(), currentFragment.getExerciseData().getType());
                } else {
                    startEvaluate(currentFragment.getSentence().getText().toString(), currentFragment.getExerciseData().getType());
                }
                return true;
            case MotionEvent.ACTION_UP:
                micro.setBackgroundResource(R.drawable.micro);
                mIse.stopEvaluating();
                return true;
        }
        return false;
    }

    public void updateTrainRecord() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                final int wordNum = DataSupport.where("lastAccess >= ?", String.valueOf(calendar.getTime().getTime())).find(LearnedWord.class).size();
                final int vocabularyNum = DataSupport.where("lastAccess >= ?", String.valueOf(calendar.getTime().getTime())).find(LearnedVocabulary.class).size();
                final int sentenceNum = DataSupport.where("lastAccess >= ?", String.valueOf(calendar.getTime().getTime())).find(LearnedSentence.class).size();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wordCount.setText(String.valueOf(wordNum + vocabularyNum) + "/" + String.valueOf(MainActivity.wordPerDay + MainActivity.vocabularyPerDay));
                        sentenceCount.setText(String.valueOf(sentenceNum) + "/" + String.valueOf(MainActivity.sentencePerDay));
                    }
                });
            }
        }).start();
    }

}
