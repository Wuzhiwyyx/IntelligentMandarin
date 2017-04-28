package com.example.wuzhi.intelligentmandarin.DataClass;

import org.litepal.crud.DataSupport;

/**
 * Created by wuzhi on 2017/3/26.
 */

public class Vocabulary extends DataSupport {
    private int id;
    private String vocabulary;
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

    public String getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(String vocabulary) {
        this.vocabulary = vocabulary;
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

    public LearnedVocabulary toLearnedVocabulary() {
        LearnedVocabulary learnedVocabulary = new LearnedVocabulary();
        learnedVocabulary.setVocabulary(vocabulary);
        learnedVocabulary.setPronounce(pronounce);
        learnedVocabulary.setTone(tone);
        learnedVocabulary.setProperty(property);
        learnedVocabulary.setExample(example);
        return learnedVocabulary;
    }
}
