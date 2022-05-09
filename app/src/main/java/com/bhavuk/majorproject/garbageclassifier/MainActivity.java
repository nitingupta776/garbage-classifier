package com.bhavuk.majorproject.garbageclassifier;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity{
    private GpsTracker gpsTracker;
    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_REQUEST_CODE = 101;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static final int IMAGE_MEAN = 128;
    public static final float IMAGE_STD = 128.0f;

    private String txtViewLatGPS;
    private String txtViewLongGPS;

    StorageReference storage_ref;
    DatabaseReference dbreff;
    FirebaseAuth mAuth;
    private StorageTask uploadTask;
    private Uri image_uri;
    DataItem data;

    private String ItemType;
    private float OrganicProbability;
    private float RecycleProbability;

    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    private Interpreter tflite;
    int[] intValues;

    Button photoButton, submitdata;
    TextView tv_result, tx1, tx2;
    ByteBuffer imgData = null;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            /* perform your actions here*/
        } else {
            signInAsAnonymous();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }


        this.imageView = (ImageView) this.findViewById(R.id.imageView1);
        photoButton = (Button) this.findViewById(R.id.btn_classify);
        submitdata = this.findViewById(R.id.btn_submit);
        tv_result = this.findViewById(R.id.tv_result);


        storage_ref = FirebaseStorage.getInstance().getReference();
        dbreff = FirebaseDatabase.getInstance().getReference().child("Data");

        data = new DataItem();

        imageView.setVisibility(View.INVISIBLE);
//        load_model();

        photoButton.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            } else {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        submitdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // startActivity(new Intent(MainActivity.this, SubmitData.class));
                upload_image_and_insert_data();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), SplashScreen.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

    private void signInAsAnonymous() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
        })
        .addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("MainActivity", "signInAnonymously:FAILURE", exception);
            }
        });
    }

    public void getLocation(){
        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            txtViewLatGPS = String.valueOf(latitude);
            txtViewLongGPS = String.valueOf(longitude);
        }else{
            gpsTracker.showSettingsAlert();
        }
    }


    private String getExtension(Uri uri){
        ContentResolver cr=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private void upload_image_and_insert_data(){
        String imageid;
        imageid = System.currentTimeMillis()+"."+getExtension(image_uri);

        data.setImageid(imageid);
        data.setItemPicked(false);
        data.setItemType(ItemType);
        data.setOrganicProbability(OrganicProbability + "%");
        data.setRecycleProbability(RecycleProbability + "%");
        data.setLatitude(txtViewLatGPS);
        data.setLongitude(txtViewLongGPS);

        dbreff.push().setValue(data);
        StorageReference Ref = storage_ref.child(imageid);
        uploadTask=Ref.putFile(image_uri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get a URL to the uploaded content
                    //Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                    Toast.makeText(MainActivity.this, "Image Uploaded Successfully!", Toast.LENGTH_LONG).show();
                    recreate();
                })
                .addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    Toast.makeText(MainActivity.this, "Failed to Upload!...Please Try again!"+ exception.getMessage(), Toast.LENGTH_LONG).show();
                    // TODO
                    // please add : after specific number of button click restart activity
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted!", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode ,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            // Get image and store it in BitMap

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            int width = photo.getWidth();
            int height = photo.getHeight();
            tv_result.setText(width + "   " + height);
            imageView.setImageBitmap(photo);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(photo, 600, 600, false);

            imageView.setImageBitmap(Bitmap.createScaledBitmap(resizedBitmap, 800, 800, false));

            int[] pix = new int[224 * 224];
            resizedBitmap.getPixels(pix, 0, 224, 0, 0, 224, 224);

            intValues = new int[224 * 224];
            try {
                tflite = new Interpreter(loadModelFile(), tfliteOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
            imgData = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);

            imgData.order(ByteOrder.nativeOrder());

            float[][] labelProbArray = new float[1][2];
            Bitmap input_image = getResizedBitmap(photo, 224, 224);

            convertBitmapToByteBuffer(input_image);

            tflite.run(imgData, labelProbArray);

            tx1 = this.findViewById(R.id.tv_result_organic);
            tx2 = this.findViewById(R.id.tv_result_recycle);

            OrganicProbability = labelProbArray[0][0] * 100;
            RecycleProbability = labelProbArray[0][1] * 100;

            tx1.setText(OrganicProbability + " %");
            tx2.setText(RecycleProbability + " %");
            if (labelProbArray[0][0] > labelProbArray[0][1]) {
                ItemType = "ORGANIC";
                tv_result.setText("ORGANIC!");
            } else{
                ItemType = "RECYCLEABLE";
                tv_result.setText("RECYCLEABLE!");
            }
            image_uri = getImageUri(this, resizedBitmap);
            imageView.setVisibility(View.VISIBLE);

            getLocation();
            
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // loop through all pixels
        int pixel = 0;
        for (int i = 0; i < 224 ; ++i) {
            for (int j = 0; j < 224; ++j) {
                final int val = intValues[pixel++];
                // get rgb values from intValues where each int holds the rgb values for a pixel.
                // if quantized, convert each rgb value to a byte, otherwise to a float

                imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
    }
    public Bitmap getResizedBitmap(Bitmap bm, int nwidth, int nheight){
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) nwidth )/width;
        float scaleHeight = ((float) nheight )/height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm,0,0,width, height,matrix,false);

        return resizedBitmap;

    }
    private MappedByteBuffer loadModelFile() throws IOException {

        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}

