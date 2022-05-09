package com.bhavuk.majorproject.garbageclassifier;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    Button btnClassifierInterface , btnMapInterface ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_main);

        btnClassifierInterface = findViewById(R.id.button) ;
        btnMapInterface = findViewById(R.id.button2);

        btnClassifierInterface.setOnClickListener(v -> {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
        });

        btnMapInterface.setOnClickListener(v -> {
            startActivity(new Intent(SplashScreen.this, MapsActivity.class));
            finish();
        });
    }
}
