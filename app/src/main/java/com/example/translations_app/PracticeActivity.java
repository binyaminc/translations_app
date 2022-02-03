package com.example.translations_app;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PracticeActivity extends AppCompatActivity {


    ArrayList<Pair> list = new ArrayList<Pair>();

    boolean discoverFlag;
    boolean swapFlag;
    int index;
    String text;//text to translate
    String viewedTran;
    String expectedTran;
    Pair pair;

    TextView disTextTextView;
    EditText disTranEditText;
    Button discoverButton, swapButton, vButton, okButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        initializeFields();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverFlag = false;

                generateAndPutNewPair();

                okButton.setVisibility(View.GONE);
            }
        });

        disTranEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                pair = (Pair) list.toArray()[index];
                text = pair.getWord();
                expectedTran = pair.getTran();
                viewedTran = disTranEditText.getText().toString();

                if (viewedTran.equals(expectedTran)) {
                    if(discoverFlag && !swapFlag){
                        //do nothing, wait until the user press on the okButton


                    }
                    else if(swapFlag){
                        discoverFlag = false;
                        swapFlag = false;
                        //in "swap", we don't need to wait time, we only swap the languages
                        generateAndPutNewPair();
                    }
                    else{
                        //disTranEditText.getBackground().setColorFilter(getResources().getColor(R.color.colorGreen),
                        //        PorterDuff.Mode.SRC_ATOP);
                        vButton.setVisibility(View.VISIBLE);
                        new CountDownTimer(500, 100) {

                            public void onTick(long millisUntilFinished) {

                            }

                            public void onFinish() {
                                //disTranEditText.getBackground().clearColorFilter();
                                vButton.setVisibility(View.GONE);
                                //the index will be different next time
                                generateAndPutNewPair();
                            }
                        }.start();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                discoverFlag = true;
                okButton.setVisibility(View.GONE);
                if(!swapFlag){
                    okButton.setVisibility(View.VISIBLE);
                }
                pair = (Pair) list.toArray()[index];
                expectedTran = pair.getTran();

                disTranEditText.setText(expectedTran);
            }
        });

        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapFlag = true;
                //swap the pairs in the pairsList
                ArrayList<Pair> tempList = new ArrayList<Pair>();
                String temp;
                for(int i = 0; i < list.size(); i++){
                    pair = (Pair) list.toArray()[i];
                    temp = pair.getWord();
                    pair.setWord(pair.getTran());
                    pair.setTran(temp);
                    tempList.add(pair);
                }
                list = tempList;

                disTranEditText.getBackground().clearColorFilter();

                discoverButton.performClick();

                disTranEditText.getBackground().clearColorFilter();
            }
        });
    }

    private void generateAndPutNewPair() {
        if (list.size() > 1) {
            int prevIndex = index;
            while (prevIndex == index)
                index = generate(0, list.size() - 1);
        }
        pair = (Pair) list.toArray()[index];
        text = pair.getWord();
        disTextTextView.setText(text);
        disTranEditText.setText("");
    }

    private void initializeFields() {
        discoverFlag = false;
        swapFlag = false;
        list = (ArrayList<Pair>) getIntent().getExtras().get("key");

        disTextTextView = (TextView) findViewById(R.id.disTextTextView);
        index = generate(0, list.size() - 1);
        pair = (Pair) list.toArray()[index];
        text = pair.getWord();

        disTextTextView.setText(text);
        disTranEditText = (EditText) findViewById(R.id.disTranEditText);

        disTranEditText.setText("");
        disTranEditText.getBackground().clearColorFilter();

        discoverButton = (Button) findViewById(R.id.discoverButton);
        swapButton = (Button) findViewById(R.id.swapButton);
        okButton = (Button) findViewById(R.id.okButton);
        okButton.setVisibility(View.GONE);
        vButton = (Button) findViewById(R.id.vButton);
        vButton.setVisibility(View.GONE);
    }

    public static int generate(int min,int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

}


