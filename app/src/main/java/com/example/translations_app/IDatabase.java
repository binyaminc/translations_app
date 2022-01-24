package com.example.translations_app;

import java.util.ArrayList;

public interface IDatabase {

    // field that declares whether the database requires any type of user authentication
    boolean databaseNeedsUserAuth = true;
    // files that declares whether the user was authenticated
    boolean databaseWasAuthed = false;

    ArrayList<Runnable> listNamesListeners = new ArrayList<>();

    String addList (String listName, ArrayList<Pair> values); // returns list identifier
    void deleteList (int index);
    void updateList (String list, ArrayList<Pair> newValues);

    ArrayList<String> getNamesOfLists ();

    Object getListRepresentation (int index); //returns UID or file with the list
    //TODO: this should return the name and the listIdentifier too
    void getTeachersList (Object listRep); //returns the list using the UID or converts the object
}

/*
functionality of IDatabase:

getNamesOfLists
getNamesAndUIDs (?)
getListRepresentation (UID / file with the list)
deleteList
getTeachersList (using UID / file)
getListByUID
addList
updateList (delete + add)

 */