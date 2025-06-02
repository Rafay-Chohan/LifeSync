package com.example.lifesync.model;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.lifesync.ExpenseFragment;
import com.example.lifesync.JournalFragment;
import com.example.lifesync.ProfileFragment;
import com.example.lifesync.TaskFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position)
        {
            case 0:
                return new TaskFragment();
            case 1:
                return new ExpenseFragment();
            case 2:
                return new JournalFragment();
            case 3:
                return new ProfileFragment();
            default:
                return new TaskFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

}
