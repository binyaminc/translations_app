package com.example.translations_app;

import java.io.Serializable;

public class Pair implements Serializable {

    String word;
    String tran;
    String comment;

    public Pair() {
        this.word = "";
        this.tran = "";
        this.comment = "";
    }
    public Pair(String word, String tran) {
        this.word = word;
        this.tran = tran;
        this.comment = "";
    }

    public Pair(String word, String tran, String comment) {
        this.word = word;
        this.tran = tran;
        this.comment = comment;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTran() {
        return tran;
    }

    public void setTran(String tran) {
        this.tran = tran;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
