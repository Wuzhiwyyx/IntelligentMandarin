package com.example.wuzhi.intelligentmandarin.DataClass;

import org.litepal.crud.DataSupport;

/**
 * Created by wuzhi on 2017/3/26.
 */

public class Word extends DataSupport{
    private int id;
    private String word;
    private String pronounce;
    private String tone;
    private String property;
    private String example;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPronounce() {
        return pronounce;
    }

    public void setPronounce(String pronounce) {
        this.pronounce = pronounce;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public LearnedWord toLearnedWord() {
        LearnedWord learnedWord = new LearnedWord();
        learnedWord.setWord(word);
        learnedWord.setPronounce(pronounce);
        learnedWord.setTone(tone);
        learnedWord.setProperty(property);
        learnedWord.setExample(example);
        return learnedWord;
    }
}
