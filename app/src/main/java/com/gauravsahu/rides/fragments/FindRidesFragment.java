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
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class FindRidesFragment extends Fragment
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap map;
    private Location mLastLocation;
    private Context context;
    private MapView mapView;
    private View ridesView;
    private Button findRidesButton;
    private Marker pickupMarker, dropMarker;
    private LatLng pickupLocation, dropLocation;
    private TextView addressView;
    private String pickupAddress, dropAddress, rideStatus;

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

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        mapView = (MapView) ridesView.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        addressView = (TextView) ridesView.findViewById(R.id.addressView);
        findRidesButton = (Button) ridesView.findViewById(R.id.findRides);

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
        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: 22-Aug-17 Request for location permissions here
            return;
        }

        //Checking location settings
        LocationRequest mLocationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        updateView();

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied,
                        // but this can be fixed by showing the user a dialog.
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
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerDragListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnMyLocationButtonClickListener(this);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        map.setMyLocationEnabled(true);
        //googleMap.setPadding(0, 0, 0, 400);
    }

    //Updates location based on the place selected from search results
    public void updateLocation(Place place) {
        switch(rideStatus) {
            case Constants.PICKUP_SELECTED:
                selectPickupLocation(place.getLatLng());
                break;

            case Constants.PICKUP_CONFIRMED:
                selectDropLocation(place.getLatLng());
                break;

            case Constants.DROP_SELECTED:
                selectDropLocation(place.getLatLng());
                break;
        }

        addressView.setText(place.getName().toString());
    }

    //Updates location based on the last known location
    public void updateView() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        selectPickupLocation(latLng);

        //Starting AddressLookupService to fetch address of last known location
        Intent intent = new Intent(context, AddressLookupService.class);
        AddressLookupResultReceiver addressReceiver = new AddressLookupResultReceiver(new Handler());
        intent.putExtra(Constants.KEY_RECEIVER, addressReceiver);
        intent.putExtra(Constants.KEY_LAST_LOCATION, latLng);
        context.startService(intent);
    }

    //Updates location address text view
    public void updateAddressTextView(String text) {
        addressView.setText(text);
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

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng latLng = marker.getPosition();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        //Starting AddressLookupService to fetch address of last known location
        AddressLookupResultReceiver addressReceiver = new AddressLookupResultReceiver(new Handler());
        Intent intent = new Intent(context, AddressLookupService.class)
                .putExtra(Constants.KEY_RECEIVER, addressReceiver)
                .putExtra(Constants.KEY_LAST_LOCATION, latLng);

        context.startService(intent);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        switch(rideStatus) {
            case Constants.PICKUP_SELECTED:
                confirmPickupLocation(marker.getPosition());
                return true;

            case Constants.DROP_SELECTED:
                confirmDropLocation(marker.getPosition());
                return true;

            default:
                return true;
        }
    }

    public void selectPickupLocation(LatLng latLng) {
        rideStatus = rideStatus == null ? Constants.PICKUP_SELECTED : rideStatus;
        pickupLocation = latLng;

        if(pickupMarker != null) {
            pickupMarker.remove();
        }

        pickupMarker = map.addMarker(new MarkerOptions().position(pickupLocation));
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.set_pickup_marker)));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 15));
    }

    public void confirmPickupLocation(LatLng latLng) {
        rideStatus = Constants.PICKUP_CONFIRMED;
        pickupLocation = latLng;
        addressView.setHint(R.string.drop_add_hint);
        pickupAddress = pickupAddress == null ? addressView.getText().toString() : pickupAddress;
        pickupMarker.setDraggable(false);
        updateAddressTextView(null);
    }

    public void unconfirmPickupLocation() {
        rideStatus = Constants.PICKUP_SELECTED;
        updateAddressTextView(pickupAddress);
        pickupMarker.setDraggable(true);
    }

    public void selectDropLocation(LatLng latLng) {
        if(dropMarker != null) {
            dropMarker.remove();
        }

        dropLocation = latLng;
        dropMarker = map.addMarker(new MarkerOptions().position(dropLocation));
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.set_pickup_marker)));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(dropLocation, 15));
        rideStatus = Constants.DROP_SELECTED;
    }

    public void deselectDropLocation() {
        rideStatus = Constants.PICKUP_CONFIRMED;
        addressView.setHint(R.string.drop_add_hint);
        pickupMarker.setDraggable(true);
        updateAddressTextView(null);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 15));

        if(dropMarker != null) {
            dropMarker.remove();
        }
    }

    public void confirmDropLocation(LatLng latLng) {
        rideStatus = Constants.DROP_CONFIRMED;
        dropLocation = latLng;
        dropAddress = dropAddress == null ? addressView.getText().toString() : dropAddress;
        addressView.setVisibility(View.GONE);
        findRidesButton.setVisibility(View.VISIBLE);
        dropMarker.setDraggable(false);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLocation).include(dropLocation);
        LatLngBounds bounds = builder.build();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }

    public void unconfirmDropLocation() {
        rideStatus = Constants.DROP_SELECTED;
        dropMarker.setDraggable(true);
        updateAddressTextView(dropAddress);
        addressView.setVisibility(View.VISIBLE);
        findRidesButton.setVisibility(View.GONE);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(dropLocation, 15));
        dropLocation = null;
    }

    public String getRideStatus() {
        return rideStatus;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        switch(rideStatus) {
            case Constants.DROP_SELECTED:
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(dropLocation, 15));
                return true;

            case Constants.DROP_CONFIRMED:
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(pickupLocation).include(dropLocation);
                LatLngBounds bounds = builder.build();
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                return true;

            default:
                return false;
        }
    }

    class AddressLookupResultReceiver extends ResultReceiver {
        AddressLookupResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch(rideStatus) {
                case Constants.PICKUP_CONFIRMED:
                    dropAddress = resultData.getString(Constants.KEY_RESULT_DATA);
                    updateAddressTextView(pickupAddress);
                    break;

                default:
                    pickupAddress = resultData.getString(Constants.KEY_RESULT_DATA);
                    updateAddressTextView(pickupAddress);
                    break;
            }
        }
    }
}