package com.bhavuk.majorproject.garbageclassifier;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.bhavuk.majorproject.garbageclassifier.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener{ //GoogleMap.OnInfoWindowClickListener

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private Bitmap bitmap = null;
    Button btnBackHome ;
    private Marker marker;
    private ImageView imageView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            /* perform your actions here*/
        } else {
            signInAsAnonymous();
        }


        btnBackHome = findViewById(R.id.button3);

        btnBackHome.setOnClickListener(v -> {
            startActivity(new Intent(MapsActivity.this, SplashScreen.class));
            finish();
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void signInAsAnonymous() {
        mAuth.signInAnonymously().addOnSuccessListener(this, (OnSuccessListener<AuthResult>) authResult -> {
            /* perform your actions here*/

        })
        .addOnFailureListener(this, (OnFailureListener) exception -> Log.e("MainActivity", "signFailed****** ", exception));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);

        //Set Custom InfoWindow Adapter
        InfoWndowAdapter adapter = new InfoWndowAdapter(MapsActivity.this);
        mMap.setInfoWindowAdapter(adapter);

        // Setting a custom info window adapter for the google map
        locateMarkers();

        // Adding and showing marker when the map is touched
        //mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(sydney);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(sydney));
        marker = mMap.addMarker(markerOptions);

        marker.showInfoWindow();

        googleMap.setOnInfoWindowClickListener(this);



    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Custom Google Marker Clicked!!!!", Toast.LENGTH_SHORT).show();

    }

    private void locateMarkers() {
        DatabaseReference currentDBcordinates = FirebaseDatabase.getInstance().getReference().child("Data");
        currentDBcordinates.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Create an array of markers
                int size = (int) dataSnapshot.getChildrenCount(); //
                Marker[] allMarkers = new Marker[size];
                mMap.clear();   //Assuming you're using mMap
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    //Specify your model class here
                    DataItem cordinatesModel = new DataItem();
                    //lets create a loop
                    for(int i=0;i<=size;i++) {
                        try {
                            //assuming you've set your getters and setters in the Model class
                            cordinatesModel.setId(ds.getValue(DataItem.class).getId());
                            cordinatesModel.setLatitude(ds.getValue(DataItem.class).getLatitude());
                            cordinatesModel.setLongitude(ds.getValue(DataItem.class).getLongitude());
                            cordinatesModel.setItemType(ds.getValue(DataItem.class).getItemType());
                            cordinatesModel.setImageid(ds.getValue(DataItem.class).getImageid());
                            cordinatesModel.setOrganicProbability(ds.getValue(DataItem.class).getOrganicProbability());
                            cordinatesModel.setRecycleProbability(ds.getValue(DataItem.class).getRecycleProbability());
                            cordinatesModel.setItemPicked(ds.getValue(DataItem.class).isItemPicked());

                            cordinatesModel.setId(ds.getKey());

                            //lets retrieve the coordinates and other information
                            String latitude1 = cordinatesModel.getLatitude();
                            String longitude1 = cordinatesModel.getLongitude();
                            String brandName=cordinatesModel.getItemType();
                            String imageid = cordinatesModel.getImageid();
                            String organicProbability = cordinatesModel.getOrganicProbability();
                            String recycleProbability = cordinatesModel.getRecycleProbability();
                            Boolean isItemPicked = cordinatesModel.isItemPicked();
                            String itemID = cordinatesModel.getId();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                                        && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                                } else {
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference storageRef = storage.getReference().child(imageid);
                                   // StorageReference storageRef = storage.getReferenceFromUrl("gs://fireapp-garbageclassifier.appspot.com/").child(imageid);

//                                    final long ONE_MEGABYTE = 1024 * 1024;
//
//                                    //download file as a byte array
//                                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
//                                        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                                       // imageView.setImageBitmap(bitmap);
//                                        Toast.makeText(MapsActivity.this, "Image downloaded from Firebase!", Toast.LENGTH_SHORT).show();
//                                    });

                                    File localFile = File.createTempFile("images", "jpg");

                                    storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            // Local temp file has been created

                                           // Toast.makeText(MapsActivity.this, "Image downloaded from Firebase!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle any errors
                                            Log.e("Error",exception.getMessage());
                                            Toast.makeText(MapsActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }

                            //convert the coordinates to LatLng
                            LatLng latLng = new LatLng(new Double(latitude1), new Double(longitude1));
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                            if(isItemPicked == true){
                                //lets add updated marker
                                allMarkers[i] = mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                        .position(latLng).title(brandName)
                                        .snippet(" Latitude: " + latitude1 + "\n Longitiude: "+ longitude1+ "\n Organic Probability: " + organicProbability + "\n Recycle Probablity: "
                                                + recycleProbability + "\n Is Item Picked: " + isItemPicked));
                            }else{
                                //lets add updated marker
                                allMarkers[i] = mMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                        .position(latLng).title(brandName)
                                        .snippet(" Latitude: " + latitude1 + "\n Longitiude: "+ longitude1+ "\n Organic Probability: " + organicProbability + "\n Recycle Probablity: "
                                                + recycleProbability + "\n Is Item Picked: " + isItemPicked));

                            }




                            mMap.setOnInfoWindowClickListener(marker -> {
                                HashMap<String, Object> result = new HashMap<>();
                            result.put("itemPicked", true);
                            FirebaseDatabase.getInstance().getReference().child("Data").child(itemID).updateChildren(result);
                                Toast.makeText(MapsActivity.this, "This item has been marked as collected!", Toast.LENGTH_SHORT).show();

                            });

                        }catch (Exception ex){}
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

}