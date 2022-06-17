package com.example.translations_app;

import java.io.Serializable;
import java.util.ArrayList;

public class myList implements Serializable {
    public String name;
    public String UID;
    public ArrayList<Pair> values;
    public Boolean isOwner;

    public myList() {
        name = "";
        UID = "";
        isOwner = false;
        values = new ArrayList<>();
    }
    public myList(String name, String UID) {
        this.name = name;
        this.UID = UID;
        isOwner = false;
        values = new ArrayList<>();
    }
    public myList(String name, String UID, Boolean isOwner) {
        this.name = name;
        this.UID = UID;
        this.isOwner = isOwner;
        values = new ArrayList<>();
    }
    public myList(String name, String UID, Boolean isOwner, ArrayList<Pair> values) {
        this.name = name;
        this.UID = UID;
        this.isOwner = isOwner;
        this.values = values;
    }
}
