package com.example.lifesync;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.os.Bundle;
import android.widget.Toast;

import com.example.lifesync.databinding.ActivityMainBinding;
import com.example.lifesync.model.ViewPagerAdapter;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;
    ActivityMainBinding binding;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter adapter;
    int currentTab = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        replaceFragment(new TaskManagerFragment());

//        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
//            switch (item.getItemId()){
//                case R.id.tmItem:
//                    replaceFragment(new TaskManagerFragment());
//                    refresh=0;
//                    break;
//                case R.id.btItem:
//                    replaceFragment(new BudgetTrackerFragment());
//                    refresh=1;
//                    break;
//                case R.id.jItem:
//                    replaceFragment(new JournalFragment());
//                    refresh=2;
//                    break;
//                case R.id.signOut:
//                    new AlertDialog.Builder(MainActivity.this)
//                            .setTitle("Sign Out")
//                            .setMessage("Are you sure you want to Sign Out?")
//                            .setPositiveButton("Yes", (d, which) -> {
//                                FirebaseAuth.getInstance().signOut();
//                                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(MainActivity.this,
//                                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
//                                googleSignInClient.signOut().addOnCompleteListener(MainActivity.this, new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        // Redirect to login activity
//                                        startActivity(new Intent(MainActivity.this,SignIn.class));
//                                    }
//                                });
//
//                            })
//                            .setNegativeButton("Cancel", null)
//                            .show();
//
//                    //startActivity(new Intent(MainActivity.this,SignIn.class));
//                    break;
//            }
//
//            return true;
//        });

        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewpager2);
        adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(
                tabLayout,
                viewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        switch (position)
                        {
                            case 0:
                                tab.setIcon(R.drawable.icon_task_manager);
                                break;
                            case 1:
                                tab.setIcon(R.drawable.icon_budget_tracker);
                                break;
                            case 2:
                                tab.setIcon(R.drawable.icon_journal);
                                break;
                            case 3:
                                tab.setIcon(R.drawable.icon_profile_tab);
                                break;
                        }
                    }
                }
        ).attach();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentTab = position;
            }
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity();
                    return;
                }
                doubleBackToExitPressedOnce = true;
                Toast.makeText(MainActivity.this, "Press again to exit", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

//        FloatingActionButton fabRefresh = findViewById(R.id.refresh);
//        fabRefresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (currentTab){
//                    case 0:
//                        replaceFragment(new TaskManagerFragment());
//                        break;
//                    case 1:
//                        replaceFragment(new BudgetTrackerFragment());
//                        break;
//                    case 2:
//                        replaceFragment(new JournalFragment());
//                        break;
//                }
//            }
        String fragmentTag = "f" + viewPager2.getCurrentItem();
        findViewById(R.id.refresh).setOnClickListener(v -> {
            Fragment fragment = getSupportFragmentManager()
                    .findFragmentByTag("f" + viewPager2.getCurrentItem());

            if (fragment instanceof RefreshableFragment) {
                ((RefreshableFragment) fragment).refreshContent();
            }
        });
    }
//    private void replaceFragment(Fragment fragment){
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.replace(R.id.viewpager2,fragment);
//        fragmentTransaction.commit();
//    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Intent intent = new Intent(MainActivity.this, SignIn.class);
            startActivity(intent);
        }
    }
}
