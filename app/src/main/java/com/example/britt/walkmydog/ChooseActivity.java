package com.example.britt.walkmydog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class ChooseActivity extends AppCompatActivity {

    Spinner spinner;

    String id;

    ListView dogList;

    private DatabaseReference databaseReference;

    ArrayList<Dog> dogArray = new ArrayList<Dog>();

    DogAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        // Set spinner to be able to choose category.
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_choose,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        dogList = findViewById(R.id.dogList);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        getFromDB();

        dogList.setOnItemClickListener(new OnItemClickListener());
//        {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                Toast.makeText(ChooseActivity.this, listItemsValue[position], Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    /**
     * Give selected category to next activity and go to next activity.
     */
    public void SelectOption(View view) {
        String option = spinner.getSelectedItem().toString();
        if (option.equals("Log Out")) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ChooseActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else if (option.equals("Overview"))
        {
            Intent intent = new Intent(ChooseActivity.this, OverviewActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void goToNext(View view) {
        Intent intent = new Intent(ChooseActivity.this, DogActivity.class);
        startActivity(intent);
    }

    public void getFromDB() {

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> ds = dataSnapshot.child("dogs").getChildren().iterator();
                while (ds.hasNext()) {

                    DataSnapshot ds1 = ds.next();

                    Dog mDog = ds1.child("dog").getValue(Dog.class);

                    dogArray.add(mDog);

                }
                mAdapter = new DogAdapter(ChooseActivity.this, dogArray);
                dogList.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseReference.addValueEventListener(eventListener);
    }

    private class OnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Dog dog = (Dog) parent.getItemAtPosition(position);

            String bossID = dog.getId();
            Log.w("TAGG", "hoi " + bossID);

            Intent intent = new Intent(ChooseActivity.this, DogActivity.class);
            intent.putExtra("bossID", bossID);
            startActivity(intent);
        }
    }
}
