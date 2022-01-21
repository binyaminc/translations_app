package com.example.translations_app;

import java.util.ArrayList;

public interface IDatabase {

    // field that declares whether the database requires any type of user authentication
    boolean databaseNeedsUserAuth = true;
    // files that declares whether the user was authenticated
    boolean databaseWasAuthed = false;

    String addList (String listName, ArrayList<Pair> values); // returns list identifier
    void deleteList (String list);
    void updateList (String list, ArrayList<Pair> newValues);

    ArrayList<String> getNamesOfLists ();

    Object getListRepresentation (String list); //returns UID or file with the list
    //TODO: this should return the name and the listIdentifier too
    myList getTeachersList (Object listRep); //returns the list using the UID or converts the object
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