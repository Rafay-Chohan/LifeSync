package com.example.lifesync;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.window.SplashScreen;
import android.os.Bundle;

import com.example.lifesync.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;
    int refresh;
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
                    refresh=0;
                    break;
                case R.id.btItem:
                    replaceFragment(new BudgetTrackerFragment());
                    refresh=1;
                    break;
                case R.id.jItem:
                    replaceFragment(new JournalFragment());
                    refresh=2;
                    break;
                case R.id.signOut:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this,SignIn.class));
                    break;
            }

            return true;
        });
        Button btn2=findViewById(R.id.refresh);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (refresh){
                    case 0:
                        replaceFragment(new TaskManagerFragment());
                        break;
                    case 1:
                        replaceFragment(new BudgetTrackerFragment());
                        break;
                    case 2:
                        replaceFragment(new JournalFragment());
                        break;
                }
            }
        });
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager =getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
}
