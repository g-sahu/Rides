package com.gauravsahu.rides.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gauravsahu.rides.R;
import com.gauravsahu.rides.fragments.AboutFragment;
import com.gauravsahu.rides.fragments.FindRidesFragment;
import com.gauravsahu.rides.fragments.UserAccountFragment;
import com.gauravsahu.rides.utilities.Constants;
import com.gauravsahu.rides.utilities.Messages;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {
    private static final String LOG_TAG = "HomeActivity";
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navDrawer;
    private final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private BroadcastReceiver netConnReceiver;
    private boolean isInternetConnected = true;
    private Fragment ridesFragment, userFragment, aboutFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        // Setup navigation drawer view
        navDrawer = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(navDrawer);

        // Tie DrawerLayout events to the ActionBarToggle
        drawerLayout.addDrawerListener(drawerToggle);

        //Setting up the default fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        ridesFragment = new FindRidesFragment();
        fragmentManager.beginTransaction().replace(R.id.fragment_content, ridesFragment, Constants.TAG_FIND_RIDES_FRAGMENT).commit();
        setTitle("Find Rides");

        //Getting currently signed in user
        FirebaseUser user = getSignedInUser();

        //Setting up Navigation Drawer header
        setupDrawerHeader(user);
    }

    @Override
    protected void onStart() {
        super.onStart();

        netConnReceiver = new NetworkConnectionReceiver();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(netConnReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(netConnReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        switch(menuItem.getItemId()) {
            case R.id.fragment_find_rides:
                ridesFragment = (ridesFragment == null) ? new FindRidesFragment() : ridesFragment;

                // Replacing/Showing fragments
                /*if(ridesFragment.isAdded()) {
                    if (userFragment != null && userFragment.isAdded()) {
                        fragmentTransaction.hide(userFragment);
                    }

                    if (aboutFragment != null && aboutFragment.isAdded()) {
                        fragmentTransaction.hide(aboutFragment);
                    }

                    fragmentTransaction.show(ridesFragment);
                } else {
                    fragmentTransaction.add(R.id.fragment_content, ridesFragment, Constants.TAG_FIND_RIDES_FRAGMENT);
                }*/

                fragmentTransaction.replace(R.id.fragment_content, ridesFragment, Constants.TAG_FIND_RIDES_FRAGMENT);

                break;

            case R.id.fragment_user_account:
                userFragment = (userFragment == null) ? new UserAccountFragment() : userFragment;

                // Replacing/Showing fragments
                if(userFragment.isAdded()) {
                    if (ridesFragment != null && ridesFragment.isAdded()) {
                        fragmentTransaction.hide(ridesFragment);
                    }

                    if (aboutFragment != null && aboutFragment.isAdded()) {
                        fragmentTransaction.hide(aboutFragment);
                    }

                    fragmentTransaction.show(userFragment);
                } else {
                    fragmentTransaction.add(R.id.fragment_content, userFragment, Constants.TAG_USER_ACCOUNT_FRAGMENT);
                }

                break;

            case R.id.fragment_about:
                aboutFragment = (aboutFragment == null) ? new AboutFragment() : aboutFragment;

                // Replacing/Showing fragments
                if(aboutFragment.isAdded()) {
                    if (userFragment != null && userFragment.isAdded()) {
                        fragmentTransaction.hide(userFragment);
                    }

                    if (ridesFragment != null && ridesFragment.isAdded()) {
                        fragmentTransaction.hide(ridesFragment);
                    }

                    fragmentTransaction.show(aboutFragment);
                } else {
                    fragmentTransaction.add(R.id.fragment_content, aboutFragment, Constants.TAG_ABOUT_FRAGMENT);
                }

                break;

            case R.id.fragment_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, SplashScreenActivity.class);
                startActivity(intent);
                finish();

            default:
                ridesFragment = (ridesFragment == null) ? new FindRidesFragment() : ridesFragment;

                // Replacing/Showing fragments
                if(ridesFragment.isAdded()) {
                    if (userFragment != null && userFragment.isAdded()) {
                        fragmentTransaction.hide(userFragment);
                    }

                    if (aboutFragment != null && aboutFragment.isAdded()) {
                        fragmentTransaction.hide(aboutFragment);
                    }

                    fragmentTransaction.show(ridesFragment);
                } else {
                    fragmentTransaction.add(R.id.fragment_content, ridesFragment, Constants.TAG_FIND_RIDES_FRAGMENT);
                }
        }

        fragmentTransaction.commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        drawerLayout.closeDrawers();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference. ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerHeader(FirebaseUser user) {
        View navHeaderView = navDrawer.getHeaderView(0);
        CircleImageView profilePicView = (CircleImageView) navHeaderView.findViewById(R.id.profile_pic);
        TextView profileNameView = (TextView) navHeaderView.findViewById(R.id.profile_name);

        //Setting profile photo
        Glide.with(this)
             .load(user.getPhotoUrl())
             .into(profilePicView);

        //Setting profile name
        profileNameView.setText(user.getDisplayName());
    }

    private FirebaseUser getSignedInUser() {
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        return fAuth.getCurrentUser();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navDrawer)) {
            drawerLayout.closeDrawer(navDrawer);
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_content);
            String fragmentTag = fragment.getTag();

            switch (fragmentTag) {
                case Constants.TAG_FIND_RIDES_FRAGMENT:
                    FindRidesFragment findRidesFragment = (FindRidesFragment) fragment;

                    switch (findRidesFragment.getRideStatus()) {
                        case Constants.PICKUP_CONFIRMED:
                            findRidesFragment.unconfirmPickupLocation();
                            break;

                        case Constants.DROP_SELECTED:
                            findRidesFragment.deselectDropLocation();
                            break;

                        case Constants.DROP_CONFIRMED:
                            findRidesFragment.unconfirmDropLocation();
                            break;

                        default:
                            super.onBackPressed();
                    }

                    break;

                case Constants.TAG_USER_ACCOUNT_FRAGMENT:

                case Constants.TAG_ABOUT_FRAGMENT:
                    selectDrawerItem(navDrawer.getMenu().getItem(0));
                    break;
            }
        }
    }

    public void showSearchActivity(View view) {
        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().build();
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    //.setBoundsBias(new LatLngBounds(new LatLng(-33.880490, 151.184363), new LatLng(-33.858754, 151.229596)))
                    .setFilter(typeFilter)
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);

                    FindRidesFragment fragment = (FindRidesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
                    fragment.updateLocation(place);

                    Log.i(LOG_TAG, "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    // TODO: Handle the error.
                    Log.i(LOG_TAG, status.getStatusMessage());

                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }

                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                if (resultCode == RESULT_OK) {
                    FindRidesFragment fragment = (FindRidesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_content);
                    fragment.updateView();
                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
        }
    }

    private void showSnackbar(String message, int length) {
        View view = findViewById(R.id.fragment_content);
        Snackbar snackbar = Snackbar.make(view, message, length);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.appTheme));
        textView.setTextSize(16);
        snackbar.show();
    }

    public class NetworkConnectionReceiver extends BroadcastReceiver {
        public NetworkConnectionReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            boolean isFailOver = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            if(isConnected) {
                if(!isInternetConnected) {
                    showSnackbar(Messages.INTERNET_CONNECTED, Snackbar.LENGTH_LONG);
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_content);

                    if(fragment.getTag().equals(Constants.TAG_FIND_RIDES_FRAGMENT)) {
                        FindRidesFragment findRidesFragment = (FindRidesFragment) fragment;
                        findRidesFragment.updateView();
                    }
                }
            } else {
                if(!isFailOver) {
                    showSnackbar(Messages.NO_INTERNET, Snackbar.LENGTH_INDEFINITE);
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_content);

                    if(fragment.getTag().equals(Constants.TAG_FIND_RIDES_FRAGMENT)) {
                        FindRidesFragment findRidesFragment = (FindRidesFragment) fragment;
                        findRidesFragment.updateAddressTextView(null);
                    }
                }
            }

            isInternetConnected = isConnected;
        }
    }
}
