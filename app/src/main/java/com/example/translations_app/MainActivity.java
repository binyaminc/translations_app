package com.example.translations_app;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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


public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private DatabaseReference myListsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userType;

    private ListView listView;
    private ArrayList<String> arrayListOfLists;
    private HashMap<String, String> mapOfListAndKey;
    private ArrayList<Pair> list;
    private ArrayAdapter<String> adapter;
    private String actionType;
    private String listLink;

    public static String listChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check validation
        if((FirebaseAuth.getInstance().getCurrentUser() == null))
        {
            sendUserToRegisterActivity();
            finish();
            return;
        }
        
        //TODO: that might be the cause to the fact that it goes to the register activity when is start-
        // because he needs to have an Extra from an intent.
        // the question is how to make it check its userType alone
        if(!getIntent().hasExtra("userType"))
        {
            sendUserToRegisterActivity();
            finish();
            return;
        }

        initializeFields();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listChoice = arrayListOfLists.get(position);
                switch(actionType) {
                    case "practice_list":
                        sendUserToPracticeActivity(listChoice);
                        break;
                    case "delete_list":
                        //ask the user if he sure he wants to delete the list, and if do - delete
                        doesWantToDelete();
                        break;
                    case "update_list":
                        //update_list
                        sendUserToUpdateListActivity(listChoice);
                        break;
                    case "share_list":
                        shareList(listChoice);
                        break;
                    default:
                        break;
                }
                actionType = "practice_list";
            }
        });





    //TODO: enter -> enterValue
    //TODO: use hebrew
    //TODO: create an opportunity to translate words using google translate or morfix
    //TODO: in a translation, maybe to suggest him the automatic translation of G"T or morfix
    //TODO: list of lists to learn, he chooses one and presses on a button- either delete, or practice or update(add or delete one pair)
    }

    private void initializeFields() {
        actionType = "practice_list";
        listLink = "";

        listView = (ListView) findViewById(R.id.myListView);

        arrayListOfLists = new ArrayList<String>();
        list = new ArrayList<Pair>();
        mapOfListAndKey = new HashMap<>();
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, arrayListOfLists);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


        userType = (String) getIntent().getExtras().get("userType");
        String generalType = (userType.equals("teacher") ? "teachers" : "students");

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        //userRef = database.getReference(currentUser.getUid());
        userRef = database.getReference().child(generalType).child(currentUser.getUid());
        myListsRef = userRef.child("myLists").getRef();

        myListsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                while(it.hasNext()){
                    DataSnapshot dataSnapshotChild = it.next();
                    arrayListOfLists.add(dataSnapshotChild.getValue().toString());
                    mapOfListAndKey.put(dataSnapshotChild.getValue().toString(), dataSnapshotChild.getKey());//value-home, key-23h94f3jdslnge
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Toast.makeText(getApplicationContext(),"didn't succeed reading lists",Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void shareList(String choice) {
        String listUId = mapOfListAndKey.get(choice);
        //copy list link
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("list link", listUId);
        clipboard.setPrimaryClip(clip);

        //dialog with "copied message!"
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        //alertDialog.setTitle("link was copied");
        alertDialog.setMessage("the link to the list " + choice + " was copied.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();

    }

    private void doesWantToDelete() {
        //ask the user if he sure he wants to delete the list, and if do - delete
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        //alertDialog.setTitle("link was copied");
        alertDialog.setMessage("Are you sure you want to delete the list \"" + listChoice + "\"? (consequences will be dire!)");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteList(listChoice);
            }
        });
        alertDialog.show();
    }

    private void deleteList(String choice) {

        arrayListOfLists.remove(choice);
        adapter.clear();
        adapter.notifyDataSetChanged();

        //remove from the private database
        String listUId = mapOfListAndKey.get(choice);
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


        //remove from the public database
        final DatabaseReference listRef = database.getReference().child("lists").child(listUId).getRef();
        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                while(it.hasNext()){
                    DataSnapshot dataSnapshotChild = it.next();
                    String [] details = dataSnapshotChild.getKey().split("__");
                    String saveableEmail = ((currentUser.getEmail()).replace('@', '_')).replace('.', '_');
                    if(details[1].equals(saveableEmail))
                        listRef.removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mapOfListAndKey.remove(choice);
    }

    private void getTeachersList() {
        //open dialog with place to enter the link from the teacher
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter list link");

        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listLink = input.getText().toString();
                updateListAfterLink();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

        //--------------------trying puting two editTexts in the dialog------------------------
        /*
        //open dialog with place to enter the link from the teacher
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("enter new pair:");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add a TextView here for the "Title" label, as noted in the comments
        final EditText textEditText = new EditText(this);
        textEditText.setHint("word");
        layout.addView(textEditText); // Notice this is an add method

        // Add another TextView here for the "Translation" label
        final EditText tranEditText = new EditText(this);
        tranEditText.setHint("translation");
        layout.addView(tranEditText); // Another add method

        builder.setView(layout); // Again this is a set method, not add


        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), textEditText.getText().toString() + " " + tranEditText.getText().toString(), Toast.LENGTH_LONG).show();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
        */
    }

    private void updateListAfterLink() {

        //if there is a link- we should update it in the lists
        if(!listLink.isEmpty()) {

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
                            mapOfListAndKey.put(listLink, listName);

                            arrayListOfLists.add(listName);
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
                    //Toast.makeText(getApplicationContext(), "didn't succeed reading list name from database", Toast.LENGTH_SHORT).show();
                    int x = 0;
                }
            });
        }
        else
            Toast.makeText(getApplicationContext(), "the link must have a content...", 0).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(userType.equals("teacher")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu_teacher, menu);
            return true;
        }
        else {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu_student, menu);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.create_new_list:
                actionType = "practice_list";
                sendUserToCreateListActivity();
                return true;
            case R.id.delete_list:
                actionType = "delete_list";
                Toast.makeText(getApplicationContext(), "Which list do you want to delete?", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.update_list:
                actionType = "update_list";
                Toast.makeText(getApplicationContext(), "Which list do you want to update?", Toast.LENGTH_SHORT).show();
                return true;
                /* */
            case R.id.share_list:
                actionType = "share_list";
                Toast.makeText(getApplicationContext(), "Which list do you want to share?", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.get_teachers_list:
                getTeachersList();
                break;

            case R.id.logOut:
                mAuth.signOut();
                sendUserToRegisterActivity();
        }
        return false;
    }


    private void sendUserToCreateListActivity() {
        Intent createListIntent = new Intent(getApplicationContext(), CreateListActivity.class);
        createListIntent.putExtra("userType", userType);
        startActivity(createListIntent);
    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void sendUserToUpdateListActivity(String choice) {
        list = new ArrayList<Pair>();
        String listUId = mapOfListAndKey.get(choice);
        final DatabaseReference listRef = database.getReference().child("lists").child(listUId).getRef();

        listRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                        String name_email = dataSnapshotChild.getKey();
                        String saveableOwnerEmail = name_email.split("__")[1];
                        String ownerEmail = saveableOwnerEmail.replace('_', '.').replaceFirst("\\.", "@");
                        //makes sure the user is the owner of the list
                        if (!ownerEmail.equals(currentUser.getEmail())) {

                            Toast.makeText(getApplicationContext(), "you can't update a list when you aren't the owner of the list", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //getting the list of words
                        for (DataSnapshot dataSnapshotWord : dataSnapshotChild.getChildren()) {

                            String comment = dataSnapshotWord.child("comment").getValue().toString();
                            String word = dataSnapshotWord.child("word").getValue().toString();
                            String tran = dataSnapshotWord.child("tran").getValue().toString();
                            list.add(new Pair(word, tran, comment));
                        }
                        //send to updateActivity
                        Intent updateActivity = new Intent(getApplicationContext(), UpdateListActivity.class);
                        updateActivity.putExtra("key", list);
                        updateActivity.putExtra("listUID", listRef.getKey());
                        updateActivity.putExtra("name_email", name_email);
                        startActivity(updateActivity);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "the list owner deleted the list...", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToPracticeActivity(String choice) {

        list = new ArrayList<Pair>();
        String listUId = mapOfListAndKey.get(choice);
        DatabaseReference listRef = database.getReference().child("lists").child(listUId).getRef();
        listRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                    while (it.hasNext()) {
                        DataSnapshot dataSnapshotChild = it.next();
                        Iterator<DataSnapshot> itChild = dataSnapshotChild.getChildren().iterator();
                        while (itChild.hasNext()) {
                            DataSnapshot dataSnapshotGrandChild = itChild.next();
                            //Pair pair = (Pair) dataSnapshotChild.getValue(); //why isn't it working???
                            //list.add(pair);
                            String comment = dataSnapshotGrandChild.child("comment").getValue().toString();
                            String word = dataSnapshotGrandChild.child("word").getValue().toString();
                            String tran = dataSnapshotGrandChild.child("tran").getValue().toString();
                            list.add(new Pair(word, tran, comment));

                        }
                    }
                    Intent practiceIntent = new Intent(getApplicationContext(), PracticeActivity.class);
                    practiceIntent.putExtra("key", list);
                    startActivity(practiceIntent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "the list owner deleted the list...", Toast.LENGTH_LONG).show();
                    return;
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
        finish();
    }


}


