package com.example.arabsignapp;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class Session {
    private String name;
    private ArrayList<Word> sentence;
    private DocumentReference user_id;

    public Session() {
    }

    public Session(String name, ArrayList<Word> sentence, DocumentReference userId) {
        this.name = name;
        this.sentence = sentence;
        this.user_id = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Word> getSentence() {
        return sentence;
    }

    public void setSentence(ArrayList<Word> sentence) {
        this.sentence = sentence;
    }

    public DocumentReference getUser_id() {
        return user_id;
    }

    public void setUser_id(DocumentReference user_id) {
        this.user_id = user_id;
    }
}
