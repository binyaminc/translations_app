package com.example.translations_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


public class LoginActivity extends AppCompatActivity {

    FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ProgressDialog loadingBar;

    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFields();



        loginButton.setOnClickListener(new View.OnClickListener() {
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

                login(email, password);

            }
        });
    }

    private void initializeFields() {
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        loadingBar = new ProgressDialog(this);

        emailEditText = (EditText) findViewById(R.id.gmailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        loginButton = (Button) findViewById(R.id.loginButton);

        userType = "notFound";
    }

    private void login(String email, String password) {


        loadingBar.setTitle("Log In");
        loadingBar.setMessage("Please wait, while we are logging in to your account...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            //Toast.makeText(getApplicationContext(),"succeeded login" , Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            currentUser = mAuth.getCurrentUser();

                            //checks whether student or teacher
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
                        else {
                            String message = task.getException().toString();
                            Toast.makeText(getApplicationContext(),message , Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    private void sendUserToMainActivity(String userType) {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.putExtra("userType", userType);
        startActivity(mainIntent);
    }
}
