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

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private TextView alreadyHaveAnAccount;
    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private RadioGroup radioGroup;
    private RadioButton radioButton;

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

        RegisterActivity myAct = this;

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
                if(radioButton == null) {
                    Toast.makeText(getApplicationContext(), "user type is empty", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                    userType = radioButton.getText().toString();

                loadingBar.setTitle("Creating New Account");
                loadingBar.setMessage("Please wait, while we are creating new account for you...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();
                // register to the database
                ((IDatabaseWithAuth)DatabaseFactory.getDatabase()).register(email, password, userType, myAct);
            }
        });

    }


    private void initializeFields() {

        loadingBar = new ProgressDialog(this);

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
            userType = db.getUserType();
            sendUserToMainActivity();
        }
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
    }
    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.putExtra("userType", userType);
        startActivity(mainIntent);
    }

    public void afterSuccessfulRegistration() {
        loadingBar.dismiss();
        sendUserToMainActivity();
    }
    public void afterFailedRegistration(String errorMessage) {

        Toast.makeText(getApplicationContext(),errorMessage , Toast.LENGTH_LONG).show();
        loadingBar.dismiss();
    }

    public void radioButtonClick(View v)
    {
        int radioButtonId = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(radioButtonId);
    }
}
