package com.example.arabsignapp;

public class Word {
    private String word;
    private String accuracy;

    public Word(){

    }

    public Word(String word, String accuracy) {
        this.word = word;
        this.accuracy = accuracy;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

}
