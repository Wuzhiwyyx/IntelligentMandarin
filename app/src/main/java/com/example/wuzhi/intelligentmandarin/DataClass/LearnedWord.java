package com.example.wuzhi.intelligentmandarin.DataClass;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by wuzhi on 2017/3/26.
 */

public class LearnedWord extends DataSupport{
    private int id;
    private String word;
    private String pronounce;
    private String tone;
    private String property;
    private String example;
    private double score;
    private Date lastAccess;

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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    public LearnedWord() {
        score = 0.0;
    }
}
