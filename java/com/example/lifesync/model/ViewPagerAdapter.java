package com.example.lifesync.model;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.lifesync.BudgetTrackerFragment;
import com.example.lifesync.JournalFragment;
import com.example.lifesync.TaskManagerFragment;

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
                return new TaskManagerFragment();
            case 1:
                return new BudgetTrackerFragment();
            case 2:
                return new JournalFragment();
            default:
                return new TaskManagerFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
