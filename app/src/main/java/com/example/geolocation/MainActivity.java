package com.example.geolocation;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText messageEditText;
    Button sendButton;
    Button addLocationButton;
    EditText manualLocationEditText;
    Button convertButton;
    TextView locationTextView;

    String geotagData;

    private SharedPreferences sharedPreferences;
    public static final String CONVERTED_GEOTAG_KEY = "ConvertedGeotag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        addLocationButton = findViewById(R.id.addLocationButton);
        manualLocationEditText = findViewById(R.id.manualLocationEditText);
        convertButton = findViewById(R.id.convertButton);
        locationTextView = findViewById(R.id.locationTextView);

        sharedPreferences = getSharedPreferences("GeotagPrefs", Context.MODE_PRIVATE);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addManualLocation();
            }
        });

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertLocation();
            }
        });

        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    // ... (Previous code for requestLocationUpdates)

    private void sendMessage() {
        String message = messageEditText.getText().toString();

        if (geotagData != null && !geotagData.isEmpty()) {
            message += "\n\n" + geotagData;
            message += "\n" + getFormattedDate();
        }

        SmsManager smsManager = SmsManager.getDefault();
        String phoneNumber = "9714096054"; // Replace with actual recipient's phone number
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);

        Toast.makeText(this, "SMS sent with geotag data!", Toast.LENGTH_SHORT).show();
    }

    private void addManualLocation() {
        String manualLocation = manualLocationEditText.getText().toString();
        if (!manualLocation.isEmpty()) {
            geotagData = "Manual Location: " + manualLocation;
            locationTextView.setText(geotagData);
        } else {
            Toast.makeText(this, "Please enter a manual location", Toast.LENGTH_SHORT).show();
        }
    }

    private void convertLocation() {
        String locationName = manualLocationEditText.getText().toString();
        if (!locationName.isEmpty()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            try {
                List<Address> addresses = geocoder.getFromLocationName(locationName, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    double latitude = address.getLatitude();
                    double longitude = address.getLongitude();

                    geotagData = "Converted Location: " + latitude + "° N, " + longitude + "° E";
                    locationTextView.setText(geotagData);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(CONVERTED_GEOTAG_KEY, geotagData);
                    editor.apply();
                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error converting location", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a location name", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }
}
