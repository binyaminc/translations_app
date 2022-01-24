package com.example.translations_app;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;


public class myFirebaseDatabase implements IDatabase{
    private static myFirebaseDatabase ourInstance = null;//TODO: check how to do it right - I don't know yet the type

    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private DatabaseReference myListsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    public static String databaseURL = "https://translationsapp-b5184.firebaseio.com/";

    private String generalType;

    private String listLink;
    private myList list;
    private ArrayList<String> arrayListOfListNames;
    private ArrayList<String> arrayListOfKeys;
    private ArrayList<myList> arrayListOfLists;

    public static myFirebaseDatabase getInstance() {
        if (ourInstance == null)
            ourInstance = new myFirebaseDatabase();
        return ourInstance;
    }

    private myFirebaseDatabase(){//String generalType) {

        arrayListOfListNames = new ArrayList<>();
        arrayListOfKeys = new ArrayList<>();
        arrayListOfLists = new ArrayList<>();


        database = FirebaseDatabase.getInstance(databaseURL);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //check if the user is a teacher or a student
        final String currentUserUId = currentUser.getUid();
        DatabaseReference teacherRef = database.getReference().child("teachers").child(currentUserUId).getRef();
        teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists())
                    generalType = "teachers";
                else
                    generalType = "students";

                //after I know the user type, I can have a reference to it
                userRef = database.getReference().child(generalType).child(currentUser.getUid());
                myListsRef = userRef.child("myLists").getRef();

                myListsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                        while(it.hasNext()){
                            DataSnapshot dataSnapshotChild = it.next();
                            String listName = dataSnapshotChild.getValue().toString();
                            String listUID = dataSnapshotChild.getKey();

                            arrayListOfListNames.add(listName);
                            arrayListOfKeys.add(listUID);
                            arrayListOfLists.add(new myList(listName, listUID));
                        }
                        for(Runnable r : listNamesListeners) {r.run();}//update all who wanted to know about changings in arrayListOfListNames
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


    }

    @Override
    public String addList(String listName, ArrayList<Pair> values) {
        return null;
    }

    @Override
    public void deleteList(int index) {
        arrayListOfListNames.remove(index);
        for(Runnable r : listNamesListeners) {r.run();}

        //remove from the private database
        String listUId = arrayListOfKeys.get(index);
        DatabaseReference myListRef = myListsRef.child(listUId).getRef();
        myListRef.removeValue();

        //makes sure there the user will remain after the delete. makes sure there is value in the myListRef
        myListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())//there are no more arrayListOfLists in the private arrayListOfLists
                    myListsRef.setValue("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //remove from the public database, if one is the owner of the list
        final DatabaseReference listRef = database.getReference().child("arrayListOfLists").child(listUId).getRef();
        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                while(it.hasNext()){
                    DataSnapshot dataSnapshotChild = it.next();
                    String [] details = dataSnapshotChild.getKey().split("__");
                    String saveableEmail = ((currentUser.getEmail()).replace('@', '_')).replace('.', '_');
                    if(details[1].equals(saveableEmail)) // I am the owner of the list
                        listRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        arrayListOfKeys.remove(index);
    }

    @Override
    public void updateList(String list, ArrayList<Pair> newValues) {

    }

    @Override
    public ArrayList<String> getNamesOfLists() {
        return arrayListOfListNames;
    }

    @Override
    public Object getListRepresentation(int index) {
        return arrayListOfKeys.get(index);
    }

    @Override
    public void getTeachersList(Object listRep) {

        listLink = (String) listRep; // in this database, the list representation is UID

        list = new myList();
        list.UID = listLink;

        DatabaseReference listRef = database.getReference().child("arrayListOfLists").child(listLink).getRef();
        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                    while (it.hasNext()) {
                        DataSnapshot dataSnapshotChild = it.next();
                        String[] details = dataSnapshotChild.getKey().split("__");
                        String listName = details[0];
                        list.name = listName;

                        arrayListOfKeys.add(listLink);
                        arrayListOfListNames.add(listName);
                        for(Runnable r : listNamesListeners) {r.run();}

                        //add values to list
                        for (Iterator<DataSnapshot> iter = dataSnapshotChild.getChildren().iterator(); iter.hasNext(); ) {
                            DataSnapshot pair = iter.next();
                            String word = pair.child("word").getValue().toString();
                            String translation = pair.child("translation").getValue().toString();
                            String comment = pair.child("comment").getValue().toString();
                            list.values.add(new Pair(word, translation, comment));
                        }
                        arrayListOfLists.add(list);

                        myListsRef.child(listLink).setValue(listName);
                    }
                }
                else {
                    //Toast.makeText(getApplicationContext(), "invalid list link or the owner deleted the list", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}

