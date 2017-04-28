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
import com.example.wuzhi.intelligentmandarin.DataClass.Vocabulary;
import com.example.wuzhi.intelligentmandarin.DataClass.Word;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TrainWordVocabularyActivity extends AppCompatActivity {

    private TextView lastElem, nextElem, pronounce, tone, element, property, example, score, details;
    private ImageView playVoice, playExample;
    private Button last, next, micro;
    private Toast mToast;

    private SpeechSynthesizer mTts;
    private SynthesizerListener mTtsListener;
    private SpeechEvaluator mIse;
    private EvaluatorListener mEvaluatorListener;


    private double lastScore, currentScore;
    private List<LearnedWord> todayWords = new ArrayList<>();
    private List<LearnedVocabulary> todayVocabularies = new ArrayList<>();
    private List<LearnedSentence> todaySentences = new ArrayList<>();
    private int wordNum, vocabularyNum, sentenceNum;
    private String category;
    private final int WORD_FLAG = 0, VOCABULARY_FLAG = 1, SENTENCE_FLAG = 2, PARAGRAPH_FLAG = 3;
    private int FLAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_word_vocabulary);

        initView();
        initFunction();
        
    }

    public void initView() {
        Intent intent = getIntent();
        category = intent.getStringExtra("category");
        if (category.equals("read_syllable")) FLAG = WORD_FLAG;
        else if (category.equals("read_word")) FLAG = VOCABULARY_FLAG;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(intent.getStringExtra("title"));
        setSupportActionBar(toolbar);

        lastElem = (TextView) findViewById(R.id.lastElem);
        nextElem = (TextView) findViewById(R.id.nextElem);
        pronounce = (TextView) findViewById(R.id.pronounce);
        tone = (TextView) findViewById(R.id.tone);
        element = (TextView) findViewById(R.id.element);
        property = (TextView) findViewById(R.id.property);
        example = (TextView) findViewById(R.id.example);
        score = (TextView) findViewById(R.id.score);
        details = (TextView) findViewById(R.id.details);
        details.setMovementMethod(new ScrollingMovementMethod());
        playVoice = (ImageView) findViewById(R.id.playVoice);
        playVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int code = mTts.startSpeaking(element.getText().toString(), mTtsListener);
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(TrainWordVocabularyActivity.this, "合成失败，错误码" + code, Toast.LENGTH_SHORT).show();
                }
            }
        });
        playExample = (ImageView) findViewById(R.id.playExample);
        playExample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int code = mTts.startSpeaking(example.getText().toString(), mTtsListener);
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(TrainWordVocabularyActivity.this, "合成失败，错误码" + code, Toast.LENGTH_SHORT).show();
                }
            }
        });
        last = (Button) findViewById(R.id.last);
        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (category == "read_syllable") {
                    if (wordNum - 1 >= 0) {
                        wordNum--;
                        updateData(WORD_FLAG);
                    }
                } else {
                    if (vocabularyNum - 1 >= 0) {
                        vocabularyNum--;
                        updateData(VOCABULARY_FLAG);
                    }else {
                        wordNum--;
                        updateData(WORD_FLAG);
                    }
                }
            }
        });
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (category == "read_syllable") {
                    if (wordNum < todayWords.size()) {
                        wordNum++;
                    }

                } else {
                    if (vocabularyNum < todayVocabularies.size()) {
                        vocabularyNum++;
                    }

                }
                if (wordNum >= todayWords.size() && vocabularyNum >= todayVocabularies.size()) {
                    finish();
                } else {
                    if (wordNum < todayWords.size()) {
                        updateData(WORD_FLAG);
                    } else if (vocabularyNum < todayVocabularies.size()) {
                        updateData(VOCABULARY_FLAG);
                    }
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
                        mIse.startEvaluating(element.getText().toString(), null, mEvaluatorListener);
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
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        prepareData();
        updateData(WORD_FLAG);
    }

    public void initFunction() {
        mTts = SpeechSynthesizer.createSynthesizer(TrainWordVocabularyActivity.this, new InitListener() {
            @Override
            public void onInit(int i) {
                if (i != ErrorCode.SUCCESS) {
                    Toast.makeText(TrainWordVocabularyActivity.this, "初始化失败", Toast.LENGTH_SHORT).show();
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

        mIse = SpeechEvaluator.createEvaluator(TrainWordVocabularyActivity.this, null);
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
                        if (lastScore < currentScore) {
                            todayWords.get(wordNum).setScore(currentScore);
                            todayWords.get(wordNum).setLastAccess(new Date());
                            todayWords.get(wordNum).save();
                        }
                        score.setText(String.valueOf(currentScore));

                    }
//                    Toast.makeText(TrainWordVocabularyActivity.this, "Evaluate end", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(SpeechError speechError) {
                if (speechError != null) {
                    Toast.makeText(TrainWordVocabularyActivity.this, "Error:" + speechError.getErrorDescription(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        };
        setEvaluateParams();
    }


    private void setEvaluateParams() {
        mIse.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        mIse.setParameter(SpeechConstant.ISE_CATEGORY, category);
        mIse.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mIse.setParameter(SpeechConstant.RESULT_LEVEL, "complete");
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
        todayWords = DataSupport.findAll(LearnedWord.class);
        Collections.shuffle(todayWords);
        List<LearnedWord> learnedWords = new ArrayList<>();
        List<Word> words = DataSupport.findAll(Word.class);
        for (Word w : words) {
            learnedWords.add(w.toLearnedWord());
        }
        Collections.shuffle(learnedWords);
        todayWords.addAll(learnedWords);

        todayVocabularies = DataSupport.findAll(LearnedVocabulary.class);
        Collections.shuffle(todayVocabularies);
        List<LearnedVocabulary> learnedVocabularies = new ArrayList<>();
        List<Vocabulary> vocabularies = DataSupport.findAll(Vocabulary.class);
        for (Vocabulary v : vocabularies) {
            learnedVocabularies.add(v.toLearnedVocabulary());
        }
        Collections.shuffle(learnedVocabularies);
        todayVocabularies.addAll(learnedVocabularies);

        todaySentences = DataSupport.findAll(LearnedSentence.class);
        Collections.shuffle(todaySentences);
        List<LearnedSentence> learnedSentences = new ArrayList<>();
        List<Sentence> sentences = DataSupport.findAll(Sentence.class);
        for (Sentence s : sentences) {
            learnedSentences.add(s.toLearnedSentence());
        }
        Collections.shuffle(learnedSentences);
        todaySentences.addAll(learnedSentences);
    }

    public void updateData(int type) {
        details.setText("");
        switch (type) {
            case WORD_FLAG:
                category = "read_syllable";
                if (wordNum < todayWords.size()) {
                    LearnedWord w = todayWords.get(wordNum);
                    element.setText(w.getWord());
                    pronounce.setText(w.getPronounce());
                    tone.setText("声调：" + w.getTone());
                    property.setText(w.getProperty());
                    example.setText(w.getExample());
                    lastScore = w.getScore();
                    score.setText(String.valueOf(lastScore));
                    if (wordNum - 1 >= 0) {
                        lastElem.setText(todayWords.get(wordNum-1).getWord());
                    } else {
                        lastElem.setText("");
                    }
                    if (wordNum + 1 < todayWords.size()) {
                        nextElem.setText(todayWords.get(wordNum+1).getWord());
                    } else {
                        nextElem.setText("");
                    }
                }
                if (wordNum == todayWords.size() - 1) {
                    if (todayVocabularies.size() > 0) {
                        nextElem.setText(todayVocabularies.get(0).getVocabulary());
                    }
                }

                break;
            case VOCABULARY_FLAG:
                category = "read_word";
                if (vocabularyNum < todayVocabularies.size()) {
                    LearnedVocabulary v = todayVocabularies.get(vocabularyNum);
                    element.setText(v.getVocabulary());
                    pronounce.setText(v.getPronounce());
                    tone.setText("声调：" + v.getTone());
                    property.setText(v.getProperty());
                    example.setText(v.getExample());
                    lastScore = v.getScore();
                    score.setText(String.valueOf(lastScore));
                    if (vocabularyNum - 1 >= 0) {
                        lastElem.setText(todayVocabularies.get(vocabularyNum-1).getVocabulary());
                    } else {
                        lastElem.setText("");
                    }
                    if (vocabularyNum + 1 < todayVocabularies.size()) {
                        nextElem.setText(todayVocabularies.get(vocabularyNum+1).getVocabulary());
                    } else {
                        nextElem.setText("");
                    }
                }
                if (vocabularyNum == 0) {
                    if (todayWords.size() > 0) {
                        lastElem.setText(todayWords.get(todayWords.size() - 1).getWord());
                    }
                }

                break;
        }
    }
}
