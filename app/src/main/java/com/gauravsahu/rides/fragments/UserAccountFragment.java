package com.gauravsahu.rides.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gauravsahu.rides.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAccountFragment extends Fragment {
    public UserAccountFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_user_account, container, false);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        updateView(view, fAuth.getCurrentUser());
        return view;
    }

    //private void updateView(View view, GoogleSignInAccount signInAccount) {
    private void updateView(View view, FirebaseUser user) {
        CircleImageView profilePicView = (CircleImageView) view.findViewById(R.id.profile_pic);
        TextView profileNameView = (TextView) view.findViewById(R.id.profile_name);
        TextView emailView = (TextView) view.findViewById(R.id.user_email);

        //Setting profile photo
        Glide.with(this)
                .load(user.getPhotoUrl())
                .into(profilePicView);

        //Setting profile name
        profileNameView.setText(user.getDisplayName());
        emailView.setText(user.getEmail());
    }
}
