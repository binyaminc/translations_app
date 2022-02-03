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
    private static myFirebaseDatabase ourInstance = null;

    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private DatabaseReference myListsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    public static String databaseURL = "https://translationsapp-b5184.firebaseio.com/";

    private Boolean databaseWasAuthed = false;

    private String generalType;

    private String listLink;
    private myList list;
    private ArrayList<String> arrayListName_Owner; //contains the name_owner of each list, to achieve easy access

    public static myFirebaseDatabase getInstance() {
        if (ourInstance == null)
            ourInstance = new myFirebaseDatabase();
        return ourInstance;
    }

    private myFirebaseDatabase(){

        database = FirebaseDatabase.getInstance(databaseURL);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)//(databaseWasAuthed)
            initializeDatabase();
    }
    public void authenticate() {
        databaseWasAuthed = true;
        initializeDatabase();
    }
    public void signOut() {
        mAuth.signOut();
    }

    public void initializeDatabase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        arrayListName_Owner = new ArrayList<>();

        arrayListOfListNames.clear();
        arrayListOfKeys.clear();
        arrayListOfLists.clear();

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

                //after I know the user type, I can have a reference to the user
                userRef = database.getReference().child(generalType).child(currentUser.getUid());
                myListsRef = userRef.child("myLists").getRef();

                myListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                            arrayListName_Owner.add("");//saves place for later updating in getListDetails function

                            getListDetails(listUID, arrayListOfLists.size()-1);
                        }
                        for(Runnable r : listNamesListeners){
                            r.run();
                        }//update all who wanted to know about changings in arrayListOfListNames
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });


    }

    private void getListDetails(String listUID, int index) {
        /*
        1. get name_owner and save in separate list (helps to writing)
        2. fill in myList.isOwner
        3. get values
        */

        DatabaseReference listRef = database.getReference().child("lists").child(listUID).getRef();

        listRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                        //saves name_owner, to provide easy access to list
                        String name_email = dataSnapshotChild.getKey();
                        arrayListName_Owner.set(index, name_email);

                        //determines whether the user is the owner of the list
                        String saveableOwnerEmail = name_email.split("__")[1];
                        String ownerEmail = saveableOwnerEmail.replace('_', '.').replaceFirst("\\.", "@");
                        arrayListOfLists.get(index).isOwner = ownerEmail.equals(currentUser.getEmail());

                        //getting the list of words
                        for (DataSnapshot dataSnapshotWord : dataSnapshotChild.getChildren()) {

                            String comment = dataSnapshotWord.child("comment").getValue().toString();
                            String word = dataSnapshotWord.child("word").getValue().toString();
                            String tran = dataSnapshotWord.child("tran").getValue().toString();
                            arrayListOfLists.get(index).values.add(new Pair(word, tran, comment));
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    public boolean databaseNeedsUserAuth() {
        return true;
    }

    // files that declares whether the user was authenticated
    public boolean databaseWasAuthed() {
        return databaseWasAuthed;
    }

    @Override
    public void addList(String listName, ArrayList<Pair> values) {

        //adding to local database
        arrayListOfListNames.add(listName);
        for(Runnable r : listNamesListeners) {r.run();}
        String listUID = myListsRef.push().getKey(); //generate UID for the list
        arrayListOfKeys.add(listUID);
        arrayListOfLists.add(new myList(listName, listUID, true));
        String name_owner = listName + "__" + currentUser.getEmail().replace('@', '_').replace('.','_');
        arrayListName_Owner.add(name_owner);//saves place for later updating in getListDetails function

        //adding to firebase database
        myListsRef.child(listUID).setValue(listName);
        DatabaseReference listRef = database.getReference().child("lists").child(listUID).child(name_owner).getRef();
        for (Pair pair : values) {
            listRef.push().setValue(pair);
        }
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


        //remove from the public database, if one is the owner of the pairsList
        final DatabaseReference listRef = database.getReference().child("arrayListOfLists").child(listUId).getRef();
        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                while(it.hasNext()){
                    DataSnapshot dataSnapshotChild = it.next();
                    String [] details = dataSnapshotChild.getKey().split("__");
                    String saveableEmail = ((currentUser.getEmail()).replace('@', '_')).replace('.', '_');
                    if(details[1].equals(saveableEmail)) // I am the owner of the pairsList
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
    public void updateList(int index, ArrayList<Pair> newValues) {

        //save to local database
        arrayListOfLists.get(index).values = newValues;

        //save to firebase
        DatabaseReference listRef = database.getReference().child("lists").child(arrayListOfKeys.get(index)).child(arrayListName_Owner.get(index)).getRef();
        listRef.removeValue();
        for (Pair pair : newValues) {
            listRef.push().setValue(pair);
        }
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

        listLink = (String) listRep; // in this database, the pairsList representation is UID

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

                        //add values to pairsList
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
                    //Toast.makeText(getApplicationContext(), "invalid pairsList link or the owner deleted the pairsList", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}

