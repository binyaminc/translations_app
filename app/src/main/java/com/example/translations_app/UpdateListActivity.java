package com.example.translations_app;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UpdateListActivity extends AppCompatActivity {

    ListView listView;
    Button addButton;

    FirebaseDatabase database;
    DatabaseReference listRef;
    String name_owner;

    MyAdapter adapter;
    ArrayList<String> words = new ArrayList<String>();
    ArrayList<String> trans = new ArrayList<String>();
    ArrayList<Pair> list = new ArrayList<Pair>();
    Boolean hasChange;
    Boolean alertDialogPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_list);

        initializeFields();

        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                /*
                View view=listView.getChildAt(0);
                EditText editText=view.findViewById(R.id.editText);
                String string=editText.getText().toString();
                Toast.makeText(getApplicationContext(), "text of first word: "+ string, Toast.LENGTH_LONG).show();
                 */
                words.add("");
                trans.add("");
                //adapter.clear();
                adapter.notifyDataSetChanged();
                /*
                //make focus on new row. probably doesn't work because I create a new one when calling getView, and don't put it in the listView
                View row = adapter.getView(adapter.getArraySize(), null, listView);
                EditText editText = (EditText) row.findViewById(R.id.wordInUpdate);
                editText.requestFocus();
                hasChange = true;
                 */

            }
        });

    }

    private void initializeFields() {
        listView = findViewById(R.id.updateListView);
        addButton = findViewById(R.id.addItemButtonUpdateActivity);

        database = FirebaseDatabase.getInstance();
        String listUID = (String) getIntent().getExtras().get("listUID");
        listRef = database.getReference().child("lists").child(listUID).getRef();
        name_owner = (String) getIntent().getExtras().get("name_email");


        list = (ArrayList<Pair>) getIntent().getExtras().get("key");
        for (int i = 0; i<list.size(); i++) {
            //words[i] = list.get(i).getWord();
            //trans[i] = list.get(i).getTran();
            words.add(list.get(i).getWord());
            trans.add(list.get(i).getTran());

        }
        hasChange = false;
        alertDialogPressed = false;

        adapter = new MyAdapter(this, words, trans);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onPause () {
        // check if there was a change
        if (!hasChange) {
            super.onPause();
            return;
        }

        /*
        //TODO: check how to show alertDialog before the "super.onPause()" is activated and exits
        //ask the user if he wants to save changes, and if do - save to firebase
        AlertDialog alertDialog = new AlertDialog.Builder(UpdateListActivity.this).create();
        alertDialog.setMessage("Save changes?");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialogPressed = true;
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ArrayList<Pair> updatedList = new ArrayList<>();
                //creating the updated list
                for (int i = 0; i<adapter.getArraySize(); i++) {
                    updatedList.add(adapter.getPair(i));
                }
                // save to firebase
                saveListOnFirebase(updatedList);
                alertDialogPressed = true;
            }
        });
        alertDialog.show();

        while (alertDialogPressed == false) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //setOnDismissListener

        */

        //creating the updated list
        ArrayList<Pair> updatedList = new ArrayList<>();
        for (int i = 0; i<adapter.getArraySize(); i++) {
            updatedList.add(adapter.getPair(i));
        }
        // save to firebase
        saveListOnFirebase(updatedList);

        super.onPause();
    }

    public void saveListOnFirebase(ArrayList<Pair> updatedList){
        //clear the whole list
        (listRef.child(name_owner).getRef()).removeValue();

        //add each pair
        for (Pair pair : updatedList) {
            listRef.child(name_owner).push().setValue(pair);
        }
    }

    class MyAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> rWords;
        ArrayList<String> rTrans;

        MyAdapter(Context c, ArrayList<String> words, ArrayList<String>trans) {
            super(c, R.layout.row_update_list, R.id.wordInUpdate, words);
            context = c;
            this.rWords = words;
            this.rTrans = trans;
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {//position declared final, as opposed to the regular decleration

            final int my_position = position;

            //inflate (create) new view object from the xml row_update_list
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row_update_list, parent, false);

            EditText word = row.findViewById(R.id.wordInUpdate);
            EditText tran = row.findViewById(R.id.tranInUpdate);
            Button deleteButton = (Button) row.findViewById(R.id.deleteRowUpdateActivity);

            word.setText(rWords.get(position));
            word.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    rWords.set(my_position, s.toString());
                    hasChange = true;
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            tran.setText(rTrans.get(position));
            tran.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    rTrans.set(my_position, s.toString());
                    hasChange = true;
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            deleteButton.setTag(position); //save in the Button the position (index in array) of the row to delete
            deleteButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag();
                    rWords.remove(pos);
                    rTrans.remove(pos);
                    notifyDataSetChanged();
                    hasChange = true;
                }
            });

            return row;


            /*
            //Alternative version - using Holder

            final int my_position = position;
            //inspired by the next question: https://stackoverflow.com/questions/38422080/listview-with-buttons-in-list-item
            final Holder holder;

            //for some reason it doesn't work when I don't inflate each time
            //if (convertView == null) {
            //    convertView = View.inflate(context, R.layout.row_update_list, null);
            //    holder = new Holder(convertView);
            //    convertView.setTag(holder);
            //} else holder = (Holder) convertView.getTag();

            convertView = View.inflate(context, R.layout.row_update_list, null);
            holder = new Holder(convertView);
            convertView.setTag(holder);

            holder.word.setText(rWords.get(position));
            holder.word.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    rWords.set(my_position, s.toString());
                    hasChange = true;
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            holder.tran.setText(rTrans.get(position));
            holder.tran.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    rTrans.set(my_position, s.toString());
                    hasChange = true;
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            holder.deleteButton.setTag(position); //save in the Button the position (index in array) of the row to delete
            holder.deleteButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag();
                    rWords.remove(pos);
                    rTrans.remove(pos);
                    notifyDataSetChanged();
                    hasChange = true;
                }
            });

            return convertView;
            */
        }

        public int getArraySize() {
            return words.size();
        }
        public Pair getPair(int position) {
            return new Pair(words.get(position), trans.get(position));
        }
    }

    /*
    static class Holder {
        EditText word;
        EditText tran;
        Button deleteButton;

        public Holder(View v) {
            word = v.findViewById(R.id.wordInUpdate);
            tran = v.findViewById(R.id.tranInUpdate);
            deleteButton = v.findViewById(R.id.deleteRowUpdateActivity);
        }
    }
    */

    // add PLUS button
    // add MINUS button
    // check how to save changes of editText
    //TODO:
    // add dialog when row is pressed to update the values, or delete word

}
