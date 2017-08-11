package com.gauravsahu.rides.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
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
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

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
        fragmentManager.beginTransaction().replace(R.id.fragment_content, new FindRidesFragment()).commit();
        setTitle("Find Rides");
        FirebaseUser user = getSignedInUser();

        //Setting up Navigation Drawer header
        setupDrawerHeader(user);
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
        Fragment fragment;

        switch(menuItem.getItemId()) {
            case R.id.fragment_find_rides:
                fragment = new FindRidesFragment();
                break;

            case R.id.fragment_user_account:
                fragment = new UserAccountFragment();
                break;

            case R.id.fragment_about:
                fragment = new AboutFragment();
                break;

            case R.id.fragment_logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, SplashScreenActivity.class);
                startActivity(intent);
                finish();

            default:
                fragment = new FindRidesFragment();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_content, fragment).commit();

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
        drawerLayout.closeDrawer(navDrawer);
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
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
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
        }
    }
}