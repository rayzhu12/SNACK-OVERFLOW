package com.example.michelleliu.homelessapp;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;
import android.app.activity;
import android.view.View.OnClickListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.CSVFile;
import model.FireBaseCallBack;
import model.GameBoard;
import model.Shelter;
import model.ShelterManager;

/**
 * App activity
 * @author snack overflow
 */
public class AppActivity extends AppCompatActivity {

    private static final String TAG = "AppActivity";

    private FirebaseDatabase mFirebaseDatabase;
    // Creating FirebaseAuth.
    private FirebaseAuth firebaseAuth ;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private DatabaseReference secondRef;
    private String userID;

    private static final ShelterManager sm = ShelterManager.getInstance();
    private static List<Shelter> shelterList;

    private final List<String> shelterNames = new ArrayList<>();

    private int nBeds = 0;
    private String shelterName = "";
    Animation frombottom;

    private Handler frame = new Handler();
    //Velocity includes the speed and the direction of our sprite motion
    private Point sprite1Velocity;
    private Point sprite2Velocity;
    private int sprite1MaxX;
    private int sprite1MaxY;
    private int sprite2MaxX;
    private int sprite2MaxY;
    //Divide the frame by 1000 to calculate how many times per second the screen will update.
    private static final int FRAME_RATE = 20; //50 frames per second
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        Handler h = new Handler();
        ((Button)findViewById(R.id.notification)).setOnClickListener(this);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                initGfx();
            }
        }, 1000);
        if (shelterList == null) {
            InputStream inputStream = getResources().openRawResource(R.raw.stats);
            CSVFile csvFile = new CSVFile(inputStream);
            shelterList = csvFile.read();
            sm.setShelterList(shelterList);
        }

        // Adding FirebaseAuth instance to FirebaseAuth object.
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        loggedIn();

        Button updateData = findViewById(R.id.updatedata);
        updateData.setOnClickListener(view -> {
            finish();
            startActivity(new Intent(AppActivity.this, ShelterListActivity.class));
        });

        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(logOutListener);

        Button releaseBed = findViewById(R.id.release);
        releaseBed.setOnClickListener(releaseBedListener);

        frombottom = AnimationUtils.loadAnimation(this,R.anim.frombottom);
        releaseBed.setAnimation(frombottom);
        logout.setAnimation(frombottom);
        updateData.setAnimation(frombottom);
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                userID = user.getUid();
                Toast.makeText(AppActivity.this, "Successfully signed in with: "
                        + user.getEmail(), Toast.LENGTH_LONG).show();
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
        };

    }
    private Point getRandomVelocity() {
        Random r = new Random();
        int min = 1;
        int max = 5;
        int x = r.nextInt(max-min+1)+min;
        int y = r.nextInt(max-min+1)+min;
        return new Point (x,y);
    }
    private Point getRandomPoint() {
        Random r = new Random();
        int minX = 0;
        int maxX = findViewById(R.id.the_canvas).getWidth() -
                ((GameBoard)findViewById(R.id.the_canvas)).getSprite1Width();
        int x = 0;
        int minY = 0;
        int maxY = findViewById(R.id.the_canvas).getHeight() -
                ((GameBoard)findViewById(R.id.the_canvas)).getSprite1Height();
        int y = 0;
        x = r.nextInt(maxX-minX+1)+minX;
        y = r.nextInt(maxY-minY+1)+minY;
        return new Point (x,y);
    }

    private void loggedIn() {
        // On activity start check whether there is user previously logged in or not.
        if (firebaseAuth.getCurrentUser() == null) {

            // Finishing current Profile activity.
            finish();

            // If user already not log in then Redirect to MainActivity .
            Intent intent = new Intent(AppActivity.this, MainActivity.class);
            startActivity(intent);

            // Showing toast message.
            Toast.makeText(AppActivity.this, "Please Log in to continue", Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Uses the map
     * @param view the view
     */
    public void useMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void updateShelter() {
        secondRef = mFirebaseDatabase.getReference("shelters");
        secondRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("AppActivity", "secondRef");
                int cap = Integer.parseInt(dataSnapshot.child(shelterName).child("capacity")
                        .getValue(String.class));
                secondRef.child(shelterName).child("capacity")
                        .setValue(Integer.toString(nBeds + cap));
                Toast.makeText(AppActivity.this, "You've released " + nBeds
                        + " bed(s) from " + shelterName, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }


    private void readData(FireBaseCallBack firebaseCallBack) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nBeds = dataSnapshot.child(userID).child("numberOfBeds").getValue(Integer.class);
                shelterName = dataSnapshot.child(userID).child("currentShelter")
                        .getValue(String.class);
                myRef.child(userID).child("numberOfBeds").setValue(0);
                myRef.child(userID).child("currentShelter").setValue(null);
                if ((shelterName != null) && (nBeds > 0)) {
                    updateShelter();
                }
                firebaseCallBack.onCallBack(shelterNames);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        };
        myRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private final View.OnClickListener releaseBedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            myRef = mFirebaseDatabase.getReference("users");
            readData(new FireBaseCallBack() {
                @Override
                public void onCallBack(List<String> list) {

                }
            });
        }
    };

    private final View.OnClickListener logOutListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Destroying login season.
            firebaseAuth.signOut();

            // Finishing current User Profile activity.
            finish();

            // Redirect to Login Activity after click on logout button.
            Intent intent = new Intent(AppActivity.this, MainActivity.class);
            startActivity(intent);

            // Showing toast message on logout.
            Toast.makeText(AppActivity.this, "Logged Out Successfully.", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
