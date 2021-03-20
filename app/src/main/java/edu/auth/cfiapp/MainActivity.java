package edu.auth.cfiapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;


public class MainActivity extends AppCompatActivity implements ProfileFragment.SendUser, SetupFragment.SendSchedule{

    public static final String EXTRA_MESSAGE = "edu.auth.cfiapp.MEALID";
    public static final String EXTRA_PLATE = "edu.auth.cfiapp.PLATE";
    public static final String EXTRA_USER = "edu.auth.cfiapp.USER";

    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout=(TabLayout)findViewById(R.id.tabLayout);
        viewPager=(ViewPager)findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);

        //final TabAdapter adapter = new TabAdapter(this, getSupportFragmentManager(), tabLayout.getTabCount());
        final TabAdapter adapter = new TabAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void sendUser(String message) {
        String tag = "android:switcher:" + R.id.viewPager + ":" + 1;
        SetupFragment setup = (SetupFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (setup != null) {
            setup.receiveUser(message);
        }

        tag = "android:switcher:" + R.id.viewPager + ":" + 2;
        TrainingFragment training = (TrainingFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (training != null) {
            training.receiveUser(message);
        }
    }


    @Override
    public void sendSchedule(int message) {
        String tag = "android:switcher:" + R.id.viewPager + ":" + 2;
        TrainingFragment training = (TrainingFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (training != null) {
            training.receiveSchedule(message);
        }

    }
}


