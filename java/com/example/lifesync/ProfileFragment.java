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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.grpc.netty.shaded.io.netty.internal.tcnative.AsyncTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView profileName, profileEmail;
    Button signOutButton;
    private Switch themeSwitch;
    private SharedPreferences sharedPreferences;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_DARK_MODE = "dark_mode";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        themeSwitch = view.findViewById(R.id.themeSwitch);
        signOutButton = view.findViewById(R.id.btnSignOut);
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
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

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
//            if (account.getPhotoUrl() != null) {
//                Glide.with(this)
//                        .load(account.getPhotoUrl())
//                        .circleCrop()
//                        .placeholder(R.drawable.icon_profile_tab)
//                        .into(profileImage);
//            } else {
//                profileImage.setImageResource(R.drawable.icon_profile_tab);
//            }
        } else {
            // User not signed in with Google
            profileName.setText("Guest User");
            profileEmail.setText("Not signed in");
            profileImage.setImageResource(R.drawable.icon_profile_tab);
        }
    }
}