package com.example.wuzhi.intelligentmandarin.DataClass;

import org.litepal.crud.DataSupport;

import java.util.Date;

/**
 * Created by wuzhi on 2017/3/26.
 */

public class LearnedSentence extends DataSupport implements ExerciseDataSource{
    private int id;
    private String sentence;
    private double score;
    private Date lastAccess;

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

    public LearnedSentence() {
        score = 0.0;
    }

    @Override
    public int getType() {
        return LEARNED_SENTENCE;
    }

    @Override
    public void saveData(Double score) {
        setScore(score);
        setLastAccess(new Date());
        save();
    }
}
