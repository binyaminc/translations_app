package com.example.translations_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    mySQLiteDatabase mMySQLiteDatabase;

    private TextView alreadyHaveAnAccount;
    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private RadioGroup radioGroup;
    private RadioButton radioButton;

    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private ProgressDialog loadingBar;

    private String userType = "notFound";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFields();

        ifRegistered();

        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if(email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "email is empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if(password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "password is empty", Toast.LENGTH_LONG).show();
                    return;
                }
                String userType;
                if(radioButton == null) {
                    Toast.makeText(getApplicationContext(), "user type is empty", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                    userType = radioButton.getText().toString();

                register(email, password, userType);

            }
        });

    }


    private void initializeFields() {

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        loadingBar = new ProgressDialog(this);

        mMySQLiteDatabase = new mySQLiteDatabase(this);

        alreadyHaveAnAccount = (TextView) findViewById(R.id.alreadyHaveAnAccount);
        emailEditText = (EditText) findViewById(R.id.registerEmailEditText);
        passwordEditText = (EditText) findViewById(R.id.registerPasswordEditText);
        radioGroup = (RadioGroup) findViewById(R.id.registerRadioGroup);
        registerButton = (Button) findViewById(R.id.registerButton);
    }

    private void ifRegistered() {
        IDatabase db = DatabaseFactory.getDatabase();
        if(db.databaseNeedsUserAuth() && !((IDatabaseWithAuth)db).databaseWasAuthed())//(currentUser == null)
            return;
        else {
            final String currentUserUId = currentUser.getUid();
            DatabaseReference teacherRef = database.getReference().child("teachers").child(currentUserUId).getRef();
            teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists())
                        userType = "teacher";
                    else
                        userType = "student";

                    sendUserToMainActivity(userType);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        }
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
    }
    private void sendUserToMainActivity(String userType) {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.putExtra("userType", userType);
        startActivity(mainIntent);
    }


    private void register(String email, String password, final String userType)
    {

        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Please wait, while we are creating new account for you...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(getApplicationContext(),"succeeded createing User With Email" , Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                            currentUser = mAuth.getCurrentUser();
                            createNewUserDatabase(userType);

                            sendUserToMainActivity(userType);
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(getApplicationContext(),message , Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    }
                });

    }

    private void createNewUserDatabase(String userType) {
        String uniqueID = currentUser.getUid();
        /*if(userType.equals("student")) {
            userRef = database.getReference().child("students").child(uniqueID).getRef();
            userRef.child("myLists").setValue("myLists");
        }
        else {
            userRef = database.getReference().child("teachers").child(uniqueID).getRef();
            userRef.child("myLists").setValue("myLists");
        }*/
        userRef = database.getReference().child(userType + "s").child(uniqueID).getRef();
        userRef.child("myLists").setValue("myLists");
    }


    public void radioButtonClick(View v)
    {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(radioButtonId);
    }
}
