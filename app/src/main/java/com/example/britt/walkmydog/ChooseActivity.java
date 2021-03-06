package com.example.britt.walkmydog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import static com.example.britt.walkmydog.AdvertActivity.setSpinner;

public class ChooseActivity extends AppCompatActivity {

    Spinner spinner;
    ListView dogList;

    // Initialize user data.
    String id;

    // Initialize for database.
    private DatabaseReference databaseReference;
    private FirebaseAuth.AuthStateListener authStateListener;

    // Initialize for use of ListView.
    ArrayList<Dog> dogArray = new ArrayList<>();
    DogAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        checkAuthentication();

        dogList = findViewById(R.id.dogList);
        spinner = findViewById(R.id.spinnerOptions3);

        // Set spinner to be able to choose option.
        setSpinner(spinner, this, R.array.spinner_choose);

        // Get information out of database.
        databaseReference = FirebaseDatabase.getInstance().getReference();
        getFromDB();

        // Set listener on all dogs.
        dogList.setOnItemClickListener(new OnItemClickListener());
    }


    /**
     * Check if user is signed and has access to this activity.
     */
    public void checkAuthentication() {
        // Check if user is signed in.
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(ChooseActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }


    /**
     * Go to selected option in spinner.
     */
    public void SelectOption(View view) {
        String option = spinner.getSelectedItem().toString();
        switch(option) {
            // Log out when button is clicked.
            case ("Log uit"):
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ChooseActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;

            // Go to overview when button is clicked.
            case ("Uitgelaten honden"):
                Intent intent2 = new Intent(ChooseActivity.this, OverviewActivity.class);
                startActivity(intent2);
                finish();
                break;
        }
    }


    /**
     * Get data of all dogs.
     */
    public void getFromDB() {

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Iterate over all adverts of dogs.
                Iterator<DataSnapshot> ds = dataSnapshot.child("dogs").getChildren().iterator();
                while (ds.hasNext()) {

                    // Add al dogs to an array.
                    DataSnapshot ds1 = ds.next();
                    Dog mDog = ds1.child("dog").getValue(Dog.class);
                    dogArray.add(mDog);
                }

                // Show dogs in ListView with custom adapter.
                mAdapter = new DogAdapter(ChooseActivity.this, dogArray);
                dogList.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        databaseReference.addValueEventListener(eventListener);
    }

    
    /**
     * Get data of selected dog and go to next activity with the selected dog's data.
     */
    private class OnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Get selected dog.
            Dog dog = (Dog) parent.getItemAtPosition(position);
            String ownerID = dog.id;

            // Go to next activity and give the owner's id to intent.
            Intent intent = new Intent(ChooseActivity.this, DogActivity.class);
            intent.putExtra("ownerID", ownerID);
            startActivity(intent);
        }
    }
}
