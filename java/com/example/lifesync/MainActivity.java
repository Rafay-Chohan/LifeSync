package com.example.lifesync;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentManager;
import android.window.SplashScreen;
import android.os.Bundle;

import com.example.lifesync.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.core.splashscreen.SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new TaskManagerFragment());


        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            /*if (item.getItemId()==R.id.btItem){
                replaceFragment(new BudgetTrackerFragment());
            } else if (item.getItemId()==R.id.tmItem) {
                replaceFragment(new TaskManagerFragment());
            }else if (item.getItemId()==R.id.jItem) {
                replaceFragment(new JournalFragment());
            }*/
            //Possible issues with Switch statement using non constant ID, will use if/else if required
            switch (item.getItemId()){
                case R.id.tmItem:
                    replaceFragment(new TaskManagerFragment());
                    break;
                case R.id.btItem:
                    replaceFragment(new BudgetTrackerFragment());
                    break;
                case R.id.jItem:
                    replaceFragment(new JournalFragment());
                    break;
            }

            return true;
        });
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager =getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}
