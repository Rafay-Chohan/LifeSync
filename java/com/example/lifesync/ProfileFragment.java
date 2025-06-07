package com.example.lifesync;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView profileName, profileEmail;
    Button signOutButton;
    private Switch themeSwitch;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";

    public ProfileFragment() {
        // Required empty public constructor
    }
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        init(view);


        signOutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(requireActivity())
                            .setTitle("Sign Out Confirmation")
                            .setMessage("Are you sure you want to Sign Out?")
                            .setPositiveButton("Yes", (d, which) -> {
                                FirebaseAuth.getInstance().signOut();
                                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(),
                                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
                                googleSignInClient.signOut().addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        startActivity(new Intent(requireActivity(),SignIn.class));
                                        requireActivity().finish();
                                    }
                                });

                            })
                            .setNegativeButton("Cancel", null)
                            .show();

        });

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
        loadGoogleAccountInfo();

        return view;
    }
    private void loadGoogleAccountInfo() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null) {
            String name = account.getDisplayName();
            profileName.setText(name != null ? name : "No name available");

            String email = account.getEmail();
            profileEmail.setText(email != null ? email : "No email available");

            // need to add dependency for glide
            if (account.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(account.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.icon_profile_tab)
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.icon_profile_tab);
            }
        } else {
            // User not signed in with Google
            profileName.setText("Guest User");
            profileEmail.setText("Not signed in");
            profileImage.setImageResource(R.drawable.icon_profile_tab);
        }
    }
    private void init(View view){
        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        themeSwitch = view.findViewById(R.id.themeSwitch);
        signOutButton = view.findViewById(R.id.btnSignOut);

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        themeSwitch.setChecked(isDarkMode);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

    }
}