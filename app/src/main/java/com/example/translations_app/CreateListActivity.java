package com.example.translations_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CreateListActivity extends AppCompatActivity {

    EditText wordEditText, tranEditText, setNameEditText;
    Button enterButton, resetButton, finishButton;

    ArrayList<Pair> list;
    String userType;

    private IDatabase db = DatabaseFactory.getDatabase();
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private DatabaseReference myListsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_list);

        initializeFields();


        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String word = wordEditText.getText().toString();
                String tran = tranEditText.getText().toString();

                list.add(new Pair(word, tran));

                wordEditText.setText("");
                tranEditText.setText("");
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.clear();
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(list.size() >= 1) {
                    //enter to firebase
                    String setName = setNameEditText.getText().toString();

                    db.addList(setName, list);

                    sendUserToMainActivity();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "there are no values yet in the list!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void initializeFields() {
        wordEditText = (EditText) findViewById(R.id.wordEditText);
        tranEditText = (EditText) findViewById(R.id.disTranEditText);
        setNameEditText = (EditText) findViewById(R.id.setNameEditText);
        enterButton = (Button) findViewById(R.id.enterButton);
        resetButton = (Button) findViewById(R.id.resetButton);
        finishButton = (Button) findViewById(R.id.finishButton);

        list = new ArrayList<Pair>();
        userType = getIntent().getExtras().get("userType").toString();

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        String generalType = (userType.equals("teacher") ? "teachers" : "students");
        userRef = database.getReference().child(generalType).child(currentUser.getUid());
        myListsRef = userRef.child("myLists").getRef();
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.putExtra("userType", userType);
        startActivity(mainIntent);
    }
}
