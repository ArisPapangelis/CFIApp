package edu.auth.cfiapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabAdapter extends FragmentPagerAdapter {

    //private Context myContext;
    int totalTabs;

    public TabAdapter(@NonNull FragmentManager fm, int behavior, int totalTabs) {
        super(fm, behavior);
        this.totalTabs = totalTabs;
    }

    /*
    public TabAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
    }

     */

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ProfileFragment profileFragment = new ProfileFragment();
                return profileFragment;
            case 1:
                SetupFragment setupFragment = new SetupFragment();
                return setupFragment;
            case 2:
                TrainingFragment trainingFragment = new TrainingFragment();
                return trainingFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
