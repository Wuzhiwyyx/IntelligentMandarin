package com.example.wuzhi.intelligentmandarin.DataClass;

import org.litepal.crud.DataSupport;

/**
 * Created by wuzhi on 2017/3/26.
 */

public class Sentence extends DataSupport {
    private int id;
    private String sentence;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public LearnedSentence toLearnedSentence() {
        LearnedSentence learnedSentence = new LearnedSentence();
        learnedSentence.setSentence(sentence);
        return learnedSentence;
    }

}
