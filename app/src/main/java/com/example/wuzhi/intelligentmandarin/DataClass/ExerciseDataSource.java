package com.example.wuzhi.intelligentmandarin.DataClass;

/**
 * Created by wuzhi on 2017/5/4.
 */

public interface ExerciseDataSource {
    int LEARNED_WORD = 0;
    int LEARNED_VOCABULARY = 1;
    int LEARNED_SENTENCE = 2;
    int getType();
    void saveData(Double score);
}
