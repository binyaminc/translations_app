package com.example.translations_app;

import java.util.ArrayList;

public class myList {
    public String name;
    public String UID;
    public ArrayList<Pair> values;

    public myList() {
        name = "";
        UID = "";
        values = new ArrayList<>();
    }
    public myList(String name, String UID) {
        this.name = name;
        this.UID = UID;
        values = new ArrayList<>();
    }
}
