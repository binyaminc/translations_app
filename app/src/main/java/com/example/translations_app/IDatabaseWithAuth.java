package com.example.translations_app;

public interface IDatabaseWithAuth extends IDatabase {

    boolean databaseWasAuthed();

    void authenticated(); //read the data of the user, assuming that the user was authenticated

    void login(String email, String password, LoginActivity activity);
    void register(String email, String password, String aUserType, RegisterActivity activity);
    void logOut();
}
