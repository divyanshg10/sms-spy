package com.dcodestar.smsspy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    LocationListener locationListener;
    Location location;
    private static final String TAG = "MainActivity";
    LocationManager locationManager;

//    private FusedLocationProviderClient fusedLocationProviderClient;
//    private LocationRequest locationRequest;
//    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askForPermission();
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

//        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                Log.d(TAG, "onLocationChanged: update came");
                location=loc;
                locationManager.removeUpdates(locationListener);
                doLocationRelatedThings();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged: changed");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(TAG, "onProviderEnabled: true");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled: true");
            }
        };
        try {
            startService(new Intent(this, BroadcastReceiverService.class));
        }catch (Exception e){
            Log.d(TAG, "onCreate: "+e.getMessage());
        }
//        doFusedSettings();
    }

    private void askForPermission(){
        String permissions[]=new String[4];
        int permissionIndex=0;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED){
            permissions[permissionIndex++]=Manifest.permission.READ_SMS;
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS},1);
            permissions[permissionIndex++]=Manifest.permission.SEND_SMS;
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECEIVE_SMS},1);
            permissions[permissionIndex++]=Manifest.permission.RECEIVE_SMS;
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
//            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            permissions[permissionIndex++]=Manifest.permission.ACCESS_FINE_LOCATION;
        }
        ActivityCompat.requestPermissions(this,permissions,1);
    }

    public void isGPSEnabled(View v){
        Log.d(TAG, "isGPSEnabled: "+locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        Toast.makeText(this,""+locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER),Toast.LENGTH_SHORT).show();
    }

    public void openMap(View v){
        Location location=null;
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "openMap: setting updates");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
        }
    }

    private void doLocationRelatedThings(){
        if(location==null){
            Toast.makeText(this,"location is null",Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?q="+location.getLatitude()+","+location.getLongitude()));
            startActivity(intent);
        }
    }

//    private  void doFusedSettings(){
//        locationRequest=new LocationRequest();
//        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setFastestInterval(500)
//                .setInterval(1000)
//                .setMaxWaitTime(1500);
//
//        locationCallback=new LocationCallback(){
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//
//                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
//                if(locationResult!=null){
////                    updateToDatabase(locationResult.getLastLocation());
//                    location=locationResult.getLastLocation();
//                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
//                            Uri.parse("http://maps.google.com/maps?q="+location.getLatitude()+","+location.getLongitude()));
//                    startActivity(intent);
//                }else{
//                    Log.d(TAG, "onLocationResult: location is null");
//                }
//            }
//        };
//    }

//    private void letsspy() {
//        StringBuilder smsBuilder = new StringBuilder();
//        final String SMS_URI_INBOX = "content://sms/inbox";
//        final String SMS_URI_ALL = "content://sms/";
//        try {
//            Uri uri = Uri.parse(SMS_URI_INBOX);
//            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
//            Cursor cur = getContentResolver().query(uri, projection, "address='Granth'", null, "date desc");
//            if (cur.moveToFirst()) {
//                int index_Address = cur.getColumnIndex("address");
//                int index_Person = cur.getColumnIndex("person");
//                int index_Body = cur.getColumnIndex("body");
//                int index_Date = cur.getColumnIndex("date");
//                int index_Type = cur.getColumnIndex("type");
//                do {
//                    String strAddress = cur.getString(index_Address);
//                    int intPerson = cur.getInt(index_Person);
//                    String strbody = cur.getString(index_Body);
//                    long longDate = cur.getLong(index_Date);
//                    int int_Type = cur.getInt(index_Type);
//
//                    smsBuilder.append("[ ");
//                    smsBuilder.append(strAddress + ", ");
//                    smsBuilder.append(intPerson + ", ");
//                    smsBuilder.append(strbody + ", ");
//                    smsBuilder.append(longDate + ", ");
//                    smsBuilder.append(int_Type);
//                    smsBuilder.append(" ]\n\n");
//                } while (cur.moveToNext());
//
//                if (!cur.isClosed()) {
//                    cur.close();
//                    cur = null;
//                }
//            } else {
//                smsBuilder.append("no result!");
//            } // end if
//        }catch(SQLiteException ex) {
//            Log.d("SQLiteException", ex.getMessage());
//        }
//        Log.d(TAG, "letsspy: "+smsBuilder.toString());
//    }
}
