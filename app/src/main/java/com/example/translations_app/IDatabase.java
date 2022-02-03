package com.example.translations_app;

import java.util.ArrayList;

public interface IDatabase {

    // field that declares whether the database requires any type of user authentication
    boolean databaseNeedsUserAuth = true;
    // files that declares whether the user was authenticated
    boolean databaseWasAuthed = false;

    ArrayList<Runnable> listNamesListeners = new ArrayList<>();

    ArrayList<String> arrayListOfListNames = new ArrayList<>();
    ArrayList<String> arrayListOfKeys = new ArrayList<>();
    ArrayList<myList> arrayListOfLists = new ArrayList<>();

    void addList (String listName, ArrayList<Pair> values);
    void deleteList (int index);
    void updateList (int index, ArrayList<Pair> newValues);

    ArrayList<String> getNamesOfLists ();

    Object getListRepresentation (int index); //returns UID or file with the pairsList

    void getTeachersList (Object listRep); //returns the pairsList using the UID or converts the object
}

/*
functionality of IDatabase:

getNamesOfLists
getNamesAndUIDs (?)
getListRepresentation (UID / file with the pairsList)
deleteList
getTeachersList (using UID / file)
getListByUID
addList
updateList (delete + add)

 */