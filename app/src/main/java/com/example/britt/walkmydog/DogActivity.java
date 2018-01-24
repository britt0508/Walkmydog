package com.example.britt.walkmydog;

import android.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DogActivity extends AppCompatActivity implements OnMapReadyCallback {

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    Spinner spinner;

    String bossID;
    Double lat;
    Double lon;
    String dog;
    String id;

    Double latitude;
    Double longitude;

    TextView nameText;
    TextView descriptionText;
    ImageView picture;

    Dog mDog;
    User mUser;

    LatLng location;

    private DatabaseReference databaseReference;

//    int myLocationRequestCode = 100;

    private FirebaseAuth mAuth;

    private static final float DEFAULT_ZOOM = 15f;
//    vars
//    private Boolean mLocationPermissionsGranted = false;
//    private GoogleMap mMap;
//    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        id = mAuth.getCurrentUser().getUid();

        // Set spinner to be able to choose category.
        spinner = findViewById(R.id.spinner6);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_doginfo_contact,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        nameText = findViewById(R.id.dog_name);
        descriptionText = findViewById(R.id.description);
        picture = findViewById(R.id.photo);

        Intent intent = getIntent();
        bossID = intent.getStringExtra("bossID");

        getFromDB();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        if (ActivityCompat.checkSelfPermission(DogActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(DogActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, myLocationRequestCode);
//        }
//        showSettingAlert();
//        Log.d("TAGG", "LOCATIEEEE");
//        mLocationPermissionsGranted = true;
//        getLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        location = new LatLng(lat, lon);

        mMap.addMarker(new MarkerOptions().position(location)
                .title("Marker of position of the dog's boss"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));

        Log.w("logtag", "" + location);

        moveCamera(new LatLng(lat, lon),
                DEFAULT_ZOOM);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }


    public void SelectOption(View view) {
        String option = spinner.getSelectedItem().toString();
        if (option.equals("Log Out")) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DogActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (option.equals("Overview")) {
            Intent intent = new Intent(DogActivity.this, OverviewActivity.class);
            startActivity(intent);
            finish();
        } else if (option.equals("Adverts")) {
            Intent intent = new Intent(DogActivity.this, ChooseActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void getFromDB() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDog = dataSnapshot.child("dogs").child(bossID).child("dog").getValue(Dog.class);
                dog = mDog.name;
                nameText.setText(dog);
                descriptionText.setText(mDog.description);
                getImage(mDog.photo, picture);
                lat = mDog.lat;
                lon = mDog.lon;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("value failure: ", "Failed to read value.");
            }
        });
    }

    public void setFavorites() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUser = dataSnapshot.child("users").child(id).getValue(User.class);

                ArrayList<Dog> favo;
                favo = mUser.favorites;


                if (favo != null) {
                    if (!favo.contains(mDog)) {
                        favo.add(mDog);
                    }
                } else {
                    Log.d("else array", mDog.getName());
                    favo = new ArrayList<>();
                    favo.add(mDog);
                }
                Log.d("arraylist", favo.toString());
                databaseReference.child("users").child(id).child("favorites").setValue(favo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("value failure: ", "Failed to read value.");
            }
        });
    }


    static void getImage(String photo, ImageView picture) {
        if (photo == null) {
            Log.w("LOGO", "Logo is used");
        } else {
            byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            picture.setImageBitmap(decodedByte);
        }
    }

    public void makeAppointment(View view) {
        Intent intent = new Intent(DogActivity.this, ContactActivity.class);
        setFavorites();
        intent.putExtra("bossID", bossID);
        intent.putExtra("dog", dog);
        startActivity(intent);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d("TAGG", "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
}

//    public void getLocation() {
//
//        Log.d("TAGG", "getDeviceLocation: getting the devices current location");
//
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//
//        try{
//            if(mLocationPermissionsGranted){
//
//                final Task location = mFusedLocationProviderClient.getLastLocation();
//                location.addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if(task.isSuccessful()){
//                            Location currentLocation = (Location) task.getResult();
//
//                            if (!(task.getResult() == null)) {
//                                latitude = currentLocation.getLatitude();
//                                longitude = currentLocation.getLongitude();
//                            }
//
//                        }else{
//                            Log.d("TAGG", "onComplete: current location is null");
//                            Toast.makeText(DogActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        }catch (SecurityException e){
//            Log.e("TAGG", "getDeviceLocation: SecurityException: " + e.getMessage() );
//        }
//    }
//
//    public void showSettingAlert()
//    {
//        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//        if (locationProviders == null || locationProviders.equals("")) {
//            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//            alertDialog.setTitle("GPS setting!");
//            alertDialog.setMessage("GPS is not enabled, Do you want to go to settings menu? ");
//            alertDialog.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                    DogActivity.this.startActivity(intent);
//                }
//            });
//            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//                }
//            });
//            alertDialog.show();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == myLocationRequestCode) {
//
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                Toast.makeText(this, "location permission granted", Toast.LENGTH_LONG).show();
//                getLocation();
//            } else {
//
//                Toast.makeText(this, "location permission denied", Toast.LENGTH_LONG).show();
//
//            }
//        }
//    }
//}
