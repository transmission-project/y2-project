package io.transmission_project.huntertalk.everythingWithGroups;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.os.IBinder;
import android.content.Intent;
import android.Manifest;
import android.location.Location;
import android.content.pm.PackageManager;
import android.app.Service;

public class TrackingService extends Service {

    private static final String TAG = TrackingService.class.getSimpleName();
    private static String groupID, uid;
    private boolean isRunning  = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        requestLocationUpdates();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        System.out.println("MyService Completed or Stopped!!! ");

    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        try {
            groupID =intent.getExtras().getString("groupID");
            uid = intent.getExtras().getString("uid");
        }catch (NullPointerException iex){
            System.out.println("null pointer was here");
        }
        return flags;
    }


    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

//Unregister the BroadcastReceiver when the notification is tapped//

            unregisterReceiver(stopReceiver);

//Stop the Service//

            stopSelf();
        }
    };

//Initiate the request to track the device's location//

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

//Specify how often your app should request the device’s location//

        request.setInterval(30000);

//Get the most accurate location data available//

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

//If the app currently has access to the location permission...//

        if (permission == PackageManager.PERMISSION_GRANTED && isRunning) {

//...then request location updates//

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

//Get a reference to the database, so your app can perform read and write operations//
                    if (isRunning) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("groups");
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
//Save the location data to the database//
                            ref.child(groupID).child("locations").child(uid).child("longitude").setValue(location.getLongitude());
                            ref.child(groupID).child("locations").child(uid).child("latitude").setValue(location.getLatitude());
                            System.out.println("Should be updated in the database now.");
                        }
                    }
                    else{
                       return;
                    }
                }
            }, null);
        }
    }
}
