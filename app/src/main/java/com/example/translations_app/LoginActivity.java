package com.example.translations_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    //FirebaseDatabase database;
    //private FirebaseAuth mAuth;
    //private FirebaseUser currentUser;
    public ProgressDialog loadingBar;

    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    //private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFields();

        LoginActivity myAct = this;

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
                // create loading bar
                loadingBar.setTitle("Log In");
                loadingBar.setMessage("Please wait, while we are logging in to your account...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();
                // login to the database
                ((IDatabaseWithAuth)DatabaseFactory.getDatabase()).login(email, password, myAct);
            }
        });
    }

    private void initializeFields() {
        //database = FirebaseDatabase.getInstance();
        //mAuth = FirebaseAuth.getInstance();
        //currentUser = mAuth.getCurrentUser();
        loadingBar = new ProgressDialog(this);

        emailEditText = (EditText) findViewById(R.id.gmailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        loginButton = (Button) findViewById(R.id.loginButton);

        //userType = "notFound";
    }

    public void afterSuccessfulLogin(String userType) { //this function is called from IDatabaseWithAuth.login
        loadingBar.dismiss();
        sendUserToMainActivity(userType);
    }
    public void afterFailedLogin(String errorMessage) { //this function is called from IDatabaseWithAuth.login
        loadingBar.dismiss();
        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    public void sendUserToMainActivity(String userType) {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.putExtra("userType", userType);
        startActivity(mainIntent);
    }
}
