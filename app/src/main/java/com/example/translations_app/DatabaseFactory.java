package com.example.translations_app;

public class DatabaseFactory {

    public static IDatabase getDatabase() {
        return myFirebaseDatabase.getInstance();
    }
}
