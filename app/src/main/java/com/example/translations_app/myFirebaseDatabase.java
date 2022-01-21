package com.example.translations_app;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class myFirebaseDatabase implements IDatabase{
    private static final myFirebaseDatabase ourInstance = new myFirebaseDatabase();

    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private DatabaseReference myListsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    public static String databaseURL = "https://translationsapp-b5184.firebaseio.com/";
    private String userType;

    private String listLink;
    myList list;
    private ArrayList<String> arrayListOfListNames;
    private HashMap<String, String> mapOfListNameAndKey;
    private ArrayList<myList> lists;

    public static myFirebaseDatabase getInstance() {
        return ourInstance;
    }

    private myFirebaseDatabase(String generalType) {

        arrayListOfListNames = new ArrayList<>();
        mapOfListNameAndKey = new HashMap<>();
        lists = new ArrayList<>();


        database = FirebaseDatabase.getInstance(databaseURL);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
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
                    mapOfListNameAndKey.put(listName, listUID);//value-home, key-23h94f3jdslnge
                    lists.add(new myList(listName, listUID));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public String addList(String listName, ArrayList<Pair> values) {
        return null;
    }

    @Override
    public void deleteList(String choice) {
        arrayListOfListNames.remove(choice);
        //adapter.clear();
        //adapter.notifyDataSetChanged();

        //remove from the private database
        String listUId = mapOfListNameAndKey.get(choice);
        DatabaseReference myListRef = myListsRef.child(listUId).getRef();
        myListRef.removeValue();

        //makes sure there the user will remain after the delete. makes sure there is value in the myListRef
        myListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())//there are no more lists in the private lists
                    myListsRef.setValue("");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //remove from the public database, if one is the owner of the list
        final DatabaseReference listRef = database.getReference().child("lists").child(listUId).getRef();
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

        mapOfListNameAndKey.remove(choice);
    }

    @Override
    public void updateList(String list, ArrayList<Pair> newValues) {

    }

    @Override
    public ArrayList<String> getNamesOfLists() {
        return null;
    }

    @Override
    public Object getListRepresentation(String list) {
        return null;
    }

    @Override
    public myList getTeachersList(Object listRep) {

        listLink = (String) listRep;

        list = new myList();
        list.UID = listLink;

        DatabaseReference listRef = database.getReference().child("lists").child(listLink).getRef();
        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                    while (it.hasNext()) {
                        DataSnapshot dataSnapshotChild = it.next();
                        String[] details = dataSnapshotChild.getKey().split("__");
                        String listName = details[0];
                        mapOfListNameAndKey.put(listLink, listName);
                        list.name = listName;

                        arrayListOfListNames.add(listName);
                        adapter.clear();
                        adapter.notifyDataSetChanged();

                        myListsRef.child(listLink).setValue(listName);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "invalid list link or the owner deleted the list", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}

