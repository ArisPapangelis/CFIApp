package edu.auth.cfiapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;


public class MainActivity extends AppCompatActivity implements ProfileFragment.SendUser, SetupFragment.SendSchedule{

    public static final String EXTRA_MEALID = "edu.auth.cfiapp.MEALID";
    public static final String EXTRA_PLATE = "edu.auth.cfiapp.PLATE";
    public static final String EXTRA_USERID = "edu.auth.cfiapp.USER";

    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
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

    /*
    Function of interface SendUser, to send a message with the selected user's username from ProfileFragment
    to SetupFragment and TrainingFragment.
    */
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

    /*
    Function of interface SendSchedule, to send a message that notifies TrainingFragment that a new schedule has been created in SetupFragment,
    or that the currently created training schedule has been deleted.
     */
    @Override
    public void sendSchedule(int message) {
        String tag = "android:switcher:" + R.id.viewPager + ":" + 2;
        TrainingFragment training = (TrainingFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (training != null) {
            training.receiveSchedule(message);
        }
    }
}


