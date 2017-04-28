package com.example.wuzhi.intelligentmandarin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wuzhi.intelligentmandarin.DataClass.LearnedSentence;
import com.example.wuzhi.intelligentmandarin.DataClass.LearnedVocabulary;
import com.example.wuzhi.intelligentmandarin.DataClass.LearnedWord;
import com.example.wuzhi.intelligentmandarin.DataClass.Sentence;
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DailySentenceParagraphActivity extends AppCompatActivity {

    private TextView wordCount, sentenceCount, sentence, score, details;
    private ImageView playVoice;
    private Button last, micro, next;
    private Toast mToast;


    private double currentScore;
    private List<Sentence> todaySentences = MainActivity.todaySentences;
    private int wordNum, vocabularyNum, sentenceNum;
    private String category;
    private final int WORD_FLAG = 0, VOCABULARY_FLAG = 1, SENTENCE_FLAG = 2, PARAGRAPH_FLAG = 3;
    private int FLAG;
    private SpeechSynthesizer mTts;
    private SynthesizerListener mTtsListener;
    private SpeechEvaluator mIse;
    private EvaluatorListener mEvaluatorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_sentence_paragraph);
        initView();
        initFunction();
    }

    public void initView() {
        Intent intent = getIntent();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        category = intent.getStringExtra("category");
        toolbar.setTitle("每日练习");
        setSupportActionBar(toolbar);


        wordCount = (TextView) findViewById(R.id.wordCount);
        sentenceCount = (TextView) findViewById(R.id.sentenceCount);
        sentence = (TextView) findViewById(R.id.sentence);
        sentence.setMovementMethod(new ScrollingMovementMethod());
        score = (TextView) findViewById(R.id.score);
        wordNum = intent.getIntExtra("wordNum", -1);
        vocabularyNum = intent.getIntExtra("vocabularyNum", -1);
        sentenceNum = intent.getIntExtra("sentenceNum", -1);
        wordCount.setText("今日字词：" + String.valueOf(wordNum + vocabularyNum) + "/" + String.valueOf(MainActivity.wordPerDay + MainActivity.vocabularyPerDay));
        sentenceCount.setText("今日句段：" + String.valueOf(sentenceNum) + "/" + String.valueOf(MainActivity.sentencePerDay));
        details = (TextView) findViewById(R.id.details);
        details.setMovementMethod(new ScrollingMovementMethod());
        playVoice = (ImageView) findViewById(R.id.playVoice);
        playVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int code = mTts.startSpeaking(sentence.getText().toString(), mTtsListener);
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(DailySentenceParagraphActivity.this, "合成失败，错误码" + code, Toast.LENGTH_SHORT).show();
                }
            }
        });
        last = (Button) findViewById(R.id.last);
        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (sentenceNum - 1 >= 0) {
                        sentenceNum--;
                        updateData(VOCABULARY_FLAG);
                    } else {
                        Intent intent1 = new Intent();
                        setResult(RESULT_CANCELED, intent1);
                        finish();
                    }
            }
        });
        micro = (Button) findViewById(R.id.micro);
        micro.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        micro.setBackgroundResource(R.drawable.micro_pressed);
                        Log.i("TAG", "------->down");
                        mIse.startEvaluating(sentence.getText().toString(), null, mEvaluatorListener);
                        return true;
                    case MotionEvent.ACTION_UP:
                        micro.setBackgroundResource(R.drawable.micro);
                        Log.i("TAG", "------->up");
                        mIse.stopEvaluating();


                        return true;
                }
                return false;
            }
        });
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (sentenceNum < MainActivity.sentencePerDay) {
                        sentenceNum++;
                    }

                if (sentenceNum >= MainActivity.sentencePerDay) {
                    Intent intent1 = new Intent();
                    setResult(RESULT_OK, intent1);
                    finish();
                } else {
                    if (sentenceNum < MainActivity.sentencePerDay) {
                        updateData(SENTENCE_FLAG);
                    }
                }
            }
        });
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        prepareData();
        updateData(SENTENCE_FLAG);
    }

    public void initFunction() {
        mTts = SpeechSynthesizer.createSynthesizer(DailySentenceParagraphActivity.this, new InitListener() {
            @Override
            public void onInit(int i) {
                if (i != ErrorCode.SUCCESS) {
                    Toast.makeText(DailySentenceParagraphActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
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

        mIse = SpeechEvaluator.createEvaluator(DailySentenceParagraphActivity.this, null);
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
                        details.setText(getDetails(resultString));
                        currentScore = getScore(resultString);

                        LearnedSentence ls = new LearnedSentence();
                        ls.setSentence(todaySentences.get(sentenceNum).getSentence());
                        ls.setLastAccess(new Date());
                        ls.setScore(currentScore);
                        ls.save();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        int wn = DataSupport.where("lastAccess >= ?", String.valueOf(calendar.getTime().getTime())).find(LearnedWord.class).size();
                        int vn = DataSupport.where("lastAccess >= ?", String.valueOf(calendar.getTime().getTime())).find(LearnedVocabulary.class).size();
                            int sn = DataSupport.where("lastAccess >= ?", String.valueOf(calendar.getTime().getTime())).find(LearnedSentence.class).size();
                        wordCount.setText("今日字词：" + String.valueOf(wn + vn) + "/" + String.valueOf(MainActivity.vocabularyPerDay + MainActivity.vocabularyPerDay));
                        sentenceCount.setText("今日句段：" + String.valueOf(sn) + "/" + String.valueOf(MainActivity.sentencePerDay));

                        score.setText(getScore(resultString).toString());
                    }
//                    Toast.makeText(DailySentenceParagraphActivity.this, "Evaluate end", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                if (speechError != null) {
                    Toast.makeText(DailySentenceParagraphActivity.this, "Error:" + speechError.getErrorDescription(), Toast.LENGTH_SHORT).show();

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
        mTts.setParameter(SpeechConstant.VOICE_NAME, MainActivity.voicer);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTts.stopSpeaking();
        mTts.destroy();
    }

    public Double getScore(String result) {
        int pos = result.indexOf("总分：");
        String temp = result.substring(pos + 3, pos + 11);
        for (int i = 0; i < temp.length(); i++) {
            if ((temp.charAt(i) < 48 || temp.charAt(i) > 57) && temp.charAt(i) != '.') {
                temp = temp.substring(0, i);
                break;
            }
        }
        return Double.parseDouble(temp.trim());
    }

    public String getDetails(String result) {
        int pos = result.indexOf("[朗读详情]");
        return result.substring(pos + 7);
    }

    public void prepareData() {
        if (todaySentences.size() == 0) {
            List<Sentence> sentences = DataSupport.findAll(Sentence.class);
            for (Sentence s : sentences) {
                if (!DataSupport.isExist(LearnedSentence.class, "sentence = ?", s.getSentence())) {
                    if (todaySentences.size() <= MainActivity.sentencePerDay) {
                        todaySentences.add(s);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public void updateData(int type) {
        details.setText("");
        score.setText("");
        if (sentenceNum < MainActivity.sentencePerDay) {
            Sentence s = todaySentences.get(sentenceNum);
            sentence.setText(s.getSentence());
        }
    }
}
