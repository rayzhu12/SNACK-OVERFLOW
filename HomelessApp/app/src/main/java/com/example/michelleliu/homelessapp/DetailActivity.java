package com.example.michelleliu.homelessapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import model.Shelter;
import model.UserInfo;
import model.UserManager;

public class DetailActivity extends AppCompatActivity {

    private EditText numOfBeds;
    private Button reserveBeds;
    private Shelter shelter;
    private String userID;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private DatabaseReference secondRef;
    private FirebaseUser firebaseUser;
    private int[] capacity = new int[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //idk what toolbar does
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        firebaseUser = mAuth.getCurrentUser();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("DetailActivity", "onAuthStateChanged:signed_in:" + user.getUid());
                    userID = user.getUid();
                    Toast.makeText(DetailActivity.this, "Successfully signed in with: " + user.getEmail(), Toast.LENGTH_LONG).show();
                } else {
                    // User is signed out
                    Log.d("DetailActivity", "onAuthStateChanged:signed_out");
                    Toast.makeText(DetailActivity.this, "Successfully signed out.", Toast.LENGTH_LONG).show();
                }
                // ...
            }
        };

        shelter = (Shelter) getIntent().getSerializableExtra("passed shelter");
        myRef = mFirebaseDatabase.getReference("shelters");

        TextView capacityTextView = (TextView) findViewById(R.id.capacity);

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                capacityTextView.setText("Capacity: " + dataSnapshot.child(shelter.getName()).getValue(Shelter.class).getCapacity());
                Log.d("DetailActivity", "capacity updated");
                capacity[0] = Integer.parseInt(dataSnapshot.child(shelter.getName()).getValue(Shelter.class).getCapacity());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
             Log.d("DetailActivity", "failed to update capacity");
            }
            });

        // replace with something bc this looks uggo
        TextView nameTextView = (TextView) findViewById(R.id.name);
        nameTextView.setText(shelter.getName());
        TextView keyTextView = (TextView) findViewById(R.id.key);
        keyTextView.setText("Key: " + Integer.toString(shelter.getKey()));
        //TextView capacityTextView = (TextView) findViewById(R.id.capacity);
        //capacityTextView.setText("Capacity: " + shelter.getCapacity());
        TextView restrictionsTextView = (TextView) findViewById(R.id.restrictions);
        restrictionsTextView.setText("Restrictions: " + shelter.getRestriction());
        TextView coordinatesTextView = (TextView) findViewById(R.id.coordinates);
        coordinatesTextView.setText("Coordinates: (" + Float.toString(shelter.getLongitude())
                + ", " + Float.toString(shelter.getLatitude()) + ")");
        TextView addressTextView = (TextView) findViewById(R.id.address);
        addressTextView.setText("Address: " + shelter.getAddress());
        TextView specialNotesTextView = (TextView) findViewById(R.id.specialNotes);
        specialNotesTextView.setText("Special Notes: " + shelter.getSpecialNotes());
        TextView phoneNumberTextView = (TextView) findViewById(R.id.phoneNumber);
        phoneNumberTextView.setText("Phone Number: " + shelter.getPhoneNumber());

        FloatingActionButton returnToList = findViewById(R.id.fab);
        returnToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        numOfBeds = (EditText) findViewById(R.id.numBeds);
        reserveBeds = (Button) findViewById(R.id.reserve);
        reserveBeds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef = mFirebaseDatabase.getReference("users");
                int numBeds = Integer.parseInt(numOfBeds.getText().toString());

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).getValue(UserInfo.class).getNumberOfBeds() == 0 && capacity[0] - numBeds > 0) {
                            myRef.child(userID).child("numberOfBeds").setValue(numBeds);
                            Log.d("DetailActivity", "beds added");
                            showData(dataSnapshot);
                        } else {
                            Log.d("DetailActivity", "beds not added") ;
                            Toast.makeText(DetailActivity.this, "You already have beds reserved.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("DetailActivity", "failed to read value");
                    }
                });
            }
        });
    }

    private void updateShelter(int numBeds) {
        secondRef = mFirebaseDatabase.getReference("shelters");
        secondRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                secondRef.child(shelter.getName()).child("capacity").setValue(capacity[0] - numBeds);
                capacity[0] = Integer.parseInt(dataSnapshot.child(shelter.getName()).getValue(Shelter.class).getCapacity());
                Log.d("DetailActivity", "shelter cap updated");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showData(DataSnapshot dataSnapshot) {
        UserInfo uInfo = new UserInfo();
        System.out.println(dataSnapshot.child(userID));
        uInfo.setName(dataSnapshot.child(userID).getValue(UserInfo.class).getName());
        uInfo.setNumberOfBeds(dataSnapshot.child(userID).getValue(UserInfo.class).getNumberOfBeds());
        Log.d("DetailActivity", "showData: name: " + uInfo.getName());
        Log.d("DetailActivity", "showData: bed; " + uInfo.getNumberOfBeds());
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
