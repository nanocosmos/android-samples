package net.nanocosmos.bintu.demo.player.activities;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import net.nanocosmos.bintu.demo.player.Util.NetworkStateReceiver;
import net.nanocosmos.bintu.demo.player.fragments.AboutFragment;
import net.nanocosmos.bintu.demo.player.fragments.BintuFragment;
import net.nanocosmos.bintu.demo.player.fragments.RTMPFragment;
import net.nanocosmos.nanoStream.demo.player.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class InitialActivity extends AppCompatActivity   implements NetworkStateReceiver.NetworkStateReceiverListener {



    private static final boolean ICON_ONLY = false;

    private String tag = "nano_initialactivity";
    //private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FrameLayout networkStatus;
    private NetworkStateReceiver networkStateReceiver;
    private BintuFragment bintuFragment;
    private RTMPFragment rtmpFragment;
    private AboutFragment aboutFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_initial);
        setToolbar();

        bintuFragment = new BintuFragment();
        rtmpFragment = new RTMPFragment();
        aboutFragment = new AboutFragment();

        FragmentManager manager = getSupportFragmentManager();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        viewPager.addOnPageChangeListener(new CustomOnPageChangeListener());

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setupTabIcons();

        networkStatus = (FrameLayout) findViewById(R.id.networkStatus);
        networkStatus.setVisibility(View.GONE);

        networkStateReceiver = NetworkStateReceiver.getInstance();
        networkStateReceiver.addListener(this);
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(bintuFragment, getResources().getString(R.string.bintu_tab_title));
        adapter.addFragment(rtmpFragment,getResources().getString(R.string.rtmp_tab_title));
        adapter.addFragment(aboutFragment, getResources().getString(R.string.about_title));
        viewPager.setAdapter(adapter);
    }
    /**
     * This method to configure the toolbar.
     * Set a Toolbar to replace the ActionBar
     */
    public void setToolbar() {
        // Set a Toolbar to replace the ActionBar.
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        toolbar.setTitle(R.string.app_name);
//        setSupportActionBar(toolbar);
    }
    private void setupTabIcons() {
        if(ICON_ONLY) {
            tabLayout.getTabAt(0).setIcon(android.R.drawable.ic_media_play);
            tabLayout.getTabAt(2).setIcon(R.drawable.nanocosmos_white);
        }
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
//            if(ICON_ONLY) {
//                return null;
//            }else {
            return mFragTitleList.get(position);
//            }
        }
    }

    @Override
    public void networkAvailable() {
        networkStatus.setVisibility(View.GONE);
        rtmpFragment.networkAvailable(true);
        bintuFragment.networkAvailable(true);
    }

    @Override
    public void networkUnavailable() {
        networkStatus.setVisibility(View.VISIBLE);
        rtmpFragment.networkAvailable(false);
        bintuFragment.networkAvailable(false);
    }

    private class CustomOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewPager.getWindowToken(), 0);

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
//                    toolbar.setTitle(R.string.load_stream_title);
                    break;
                case 1:
//                    toolbar.setTitle(R.string.about_title);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

}
