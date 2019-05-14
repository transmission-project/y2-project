package com.example.huntertalk.everythingWithGroups;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.huntertalk.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



public class MapFragment extends Fragment {

    private GoogleMap mMap;
    private Location mLastKnownLocation;
    private Location mCurrentLocation;
    private CameraPosition mCameraPosition;
    private float DEFAULT_ZOOM= 0;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private  LatLng currentLocation;
    private double latitude, longitude;
    private DatabaseReference groupRef;
    private String uid;
    private String groupID;


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args= this.getArguments();
        groupID= args.getString("groupID");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        groupRef= database.getReference().child("groups");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
startTrackerService();
    }
    //Start the TrackerService//
    private void startTrackerService() {
        Intent service= new Intent(this.getContext(), TrackingService.class);
        service.putExtra("groupID", groupID);
        service.putExtra("uid", uid);
        getActivity().startService(service);
        System.out.println("The tracking has been enabled");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) throws SecurityException{
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.frg);  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap mMap1) {
                mMap=mMap1;
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mMap.setMyLocationEnabled(true);
                groupRef.child(groupID).child("locations").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot parameters: dataSnapshot.getChildren()){
                            System.out.println("The key is "+ parameters.getKey());
                            if(parameters.getKey().equals("latitude")){
                                latitude= Double.parseDouble(parameters.getValue().toString());
                            }
                            if(parameters.getKey().equals("longitude")){
                               longitude= Double.parseDouble(parameters.getValue().toString());
                            }
                        }
                        currentLocation= new LatLng(latitude,longitude);
                        // Add a marker in Sydney and move the camera
                       // mMap.addMarker(new MarkerOptions().position(currentLocation).title("Marker in Current Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                groupRef.child(groupID).child("locations").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mMap.clear();
                        for (DataSnapshot user: dataSnapshot.getChildren()){
                            double longitude= Double.parseDouble(user.child("longitude").getValue().toString());
                            double latitude= Double.parseDouble(user.child("latitude").getValue().toString());
                            LatLng currentLocationOfAUser= new LatLng(latitude,longitude);
                            mMap.addMarker(new MarkerOptions().position(currentLocationOfAUser).title(user.getKey()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });


        return rootView;
    }
}