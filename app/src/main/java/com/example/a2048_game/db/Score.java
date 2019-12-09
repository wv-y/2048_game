package com.example.a2048_game.db;

import java.util.Date;

public class Score {
    private int score;
    private String name;
    private Date time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Score(String name,int score,Date time){
        this.setName(name);
        this.setScore(score);
        this.setTime(time);
    }

    public Score(){
    }

}