package com.gauravsahu.rides.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravsahu.rides.R;
import com.gauravsahu.rides.services.AddressLookupService;
import com.gauravsahu.rides.utilities.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class FindRidesFragment extends Fragment
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap map;
    private Location mLastLocation;
    private Context context;
    private MapView mapView;
    private View ridesView;
    private Marker marker;

    public FindRidesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ridesView = inflater.inflate(R.layout.fragment_find_rides, container, false);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        mapView = (MapView) ridesView.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        return ridesView;
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        mapView.onStart();
        super.onStart();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Toast.makeText(context, "Connected to Google Play Services.", Toast.LENGTH_SHORT).show();

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Displaying last known location
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        //Checking location settings
        LocationRequest mLocationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult((Activity) context, LocationSettingsStatusCodes.RESOLUTION_REQUIRED);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(context, "Connection suspended!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(context, "Connection failed!", Toast.LENGTH_SHORT).show();
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        updateView();
    }

     public void updateLocation(Place place) {
         String name = place.getName().toString();
         LatLng blr = place.getLatLng();

         if(marker != null) {
            marker.remove();
         }

         marker = map.addMarker(new MarkerOptions().position(blr).title("Bengaluru"));
                                                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.set_pickup_marker)));
         map.moveCamera(CameraUpdateFactory.newLatLngZoom(blr, 15));
         TextView pickupView = (TextView) ridesView.findViewById(R.id.pickup_address);
         pickupView.setText(place.getAddress());
     }

    public void updateView() {
        LatLng blr = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        if(marker != null) {
            marker.remove();
        }

        marker = map.addMarker(new MarkerOptions().position(blr).title("Bengaluru"));
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.set_pickup_marker)));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(blr, 15));

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        map.setMyLocationEnabled(true);
        //googleMap.setPadding(0, 0, 0, 400);

        //Starting AddressLookupService to fetch address of last known location
        Intent intent = new Intent(context, AddressLookupService.class);
        AddressLookupResultReceiver addressReceiver = new AddressLookupResultReceiver(new Handler());
        intent.putExtra(Constants.KEY_RECEIVER, addressReceiver);
        intent.putExtra(Constants.KEY_LAST_LOCATION, mLastLocation);
        context.startService(intent);
    }

    public void updateTextView(String text) {
        TextView pickupView = (TextView) ridesView.findViewById(R.id.pickup_address);
        pickupView.setText(text);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    class AddressLookupResultReceiver extends ResultReceiver {
        AddressLookupResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String pickupAddress = resultData.getString(Constants.KEY_RESULT_DATA);
            updateTextView(pickupAddress);
        }
    }
}
