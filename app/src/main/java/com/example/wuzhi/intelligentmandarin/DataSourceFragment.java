package com.example.wuzhi.intelligentmandarin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wuzhi.intelligentmandarin.DataClass.ExerciseDataSource;
import com.example.wuzhi.intelligentmandarin.DataClass.LearnedSentence;
import com.example.wuzhi.intelligentmandarin.DataClass.LearnedVocabulary;
import com.example.wuzhi.intelligentmandarin.DataClass.LearnedWord;

/**
 * Created by wuzhi on 2017/5/5.
 */

public class DataSourceFragment extends Fragment implements View.OnClickListener {
    private TextView pronounce, tone, element, sentence,property, example, score, details;
    private LinearLayout wordLinearLayout, sentenceLinearLayout;
    private ExerciseDataSource exerciseData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_daily_exercise, container, false);
        wordLinearLayout = (LinearLayout) view.findViewById(R.id.wordLinearLayout);
        sentenceLinearLayout = (LinearLayout) view.findViewById(R.id.sentenceLinearLayout);
        pronounce = (TextView) view.findViewById(R.id.pronounce);
        tone = (TextView) view.findViewById(R.id.tone);
        element = (TextView) view.findViewById(R.id.element);
        element.setOnClickListener(this);
        sentence = (TextView) view.findViewById(R.id.sentence);
        sentence.setOnClickListener(this);
        sentence.setMovementMethod(new ScrollingMovementMethod());
        property = (TextView) view.findViewById(R.id.property);
        example = (TextView) view.findViewById(R.id.example);
        example.setOnClickListener(this);
        score = (TextView) view.findViewById(R.id.score);
        details = (TextView) view.findViewById(R.id.details);
        details.setMovementMethod(new ScrollingMovementMethod());
        loadData();
        return view;
    }

    public void loadData() {
        switch (exerciseData.getType()) {
            case ExerciseDataSource.LEARNED_WORD:
                wordLinearLayout.setVisibility(View.VISIBLE);
                sentenceLinearLayout.setVisibility(View.GONE);
                LearnedWord learnedWord = (LearnedWord) exerciseData;
                details.setText("");
                score.setText("");
                element.setText(learnedWord.getWord());
                pronounce.setText(learnedWord.getPronounce());
                tone.setText(learnedWord.getTone());
                property.setText(learnedWord.getProperty());
                example.setText(learnedWord.getExample());
                score.setText(String.valueOf(learnedWord.getScore()));
                break;
            case ExerciseDataSource.LEARNED_VOCABULARY:
                wordLinearLayout.setVisibility(View.VISIBLE);
                sentenceLinearLayout.setVisibility(View.GONE);
                LearnedVocabulary learnedVocabulary = (LearnedVocabulary) exerciseData;
                details.setText("");
                score.setText("");
                element.setText(learnedVocabulary.getVocabulary());
                pronounce.setText(learnedVocabulary.getPronounce());
                tone.setText(learnedVocabulary.getTone());
                property.setText(learnedVocabulary.getProperty());
                example.setText(learnedVocabulary.getExample());
                score.setText(String.valueOf(learnedVocabulary.getScore()));
                break;
            case ExerciseDataSource.LEARNED_SENTENCE:
                wordLinearLayout.setVisibility(View.GONE);
                sentenceLinearLayout.setVisibility(View.VISIBLE);
                LearnedSentence learnedSentence = (LearnedSentence) exerciseData;
                details.setText("");
                score.setText("");
                sentence.setText(learnedSentence.getSentence());
                score.setText(String.valueOf(learnedSentence.getScore()));
                break;
        }
    }

    public void setExerciseData(ExerciseDataSource exerciseData) {
        this.exerciseData = exerciseData;
    }

    public ExerciseDataSource getExerciseData() {
        return exerciseData;
    }

    public TextView getSentence() {
        return sentence;
    }

    public TextView getElement() {
        return element;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.element:
                ((TrainActivity)getActivity()).startSpeech(element.getText().toString());
                break;
            case R.id.sentence:
                ((TrainActivity)getActivity()).startSpeech(sentence.getText().toString());
                break;
            case R.id.example:
                ((TrainActivity)getActivity()).startSpeech(example.getText().toString());
                break;
        }
    }

    public void setEvaluateResult(String result) {
        Double tempScore = getScore(result);
        if (tempScore != 0.0) {
            exerciseData.saveData(tempScore);
            ((TrainActivity)getActivity()).updateTrainRecord();
        }
        score.setText(String.valueOf(getScore(result)));
        details.setText(getDetails(result));

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
        return Double.valueOf(temp.trim());
    }

    public String getDetails(String result) {
        int pos = result.indexOf("[朗读详情]");
        return result.substring(pos + 7);
    }
}
