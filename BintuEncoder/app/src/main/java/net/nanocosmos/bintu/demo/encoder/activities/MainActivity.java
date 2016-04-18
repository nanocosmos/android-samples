package net.nanocosmos.bintu.demo.encoder.activities;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import net.nanocosmos.bintu.demo.encoder.R;
import net.nanocosmos.bintu.demo.encoder.fragment.About;
import net.nanocosmos.bintu.demo.encoder.fragment.BintuApiKeyInput;
import net.nanocosmos.bintu.demo.encoder.fragment.RtmpInput;
import net.nanocosmos.bintu.demo.encoder.util.NetworkStateReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nanocosmos GmbH (c) 2015 - 2016
 */
public class MainActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {
    private static final String TAG = "MainActivity";
    private static final boolean ICON_ONLY = false;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FrameLayout networkStatus;

    private BintuApiKeyInput bintuApiKeyInput;
    private RtmpInput rtmpInput;
    private About about;

    private NetworkStateReceiver networkStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbar(); // set toolbar

        rtmpInput = new RtmpInput();
        bintuApiKeyInput = new BintuApiKeyInput();
        about = new About();

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

    /**
     * This method to configure the toolbar.
     * Set a Toolbar to replace the ActionBar
     */
    public void setToolbar() {
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.bintu_title);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        networkStateReceiver.removeListener(this);
        unregisterReceiver(networkStateReceiver);
        super.onBackPressed();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(bintuApiKeyInput, getResources().getString(R.string.bintu_tab));
        adapter.addFragment(rtmpInput, getResources().getString(R.string.rtmp_tab));
        adapter.addFragment(about, getResources().getString(R.string.about_tab));
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        if (ICON_ONLY) {
            tabLayout.getTabAt(0).setIcon(android.R.drawable.ic_media_play);
            tabLayout.getTabAt(1).setIcon(android.R.drawable.ic_media_next);
            tabLayout.getTabAt(2).setIcon(R.drawable.ic_nanocosmos_white);
        }
    }

    @Override
    public void networkAvailable() {
        networkStatus.setVisibility(View.GONE);
        rtmpInput.networkAvailable(true);
        bintuApiKeyInput.networkAvailable(true);
    }

    @Override
    public void networkUnavailable() {
        networkStatus.setVisibility(View.VISIBLE);
        rtmpInput.networkAvailable(false);
        bintuApiKeyInput.networkAvailable(false);
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

    private class CustomOnPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    toolbar.setTitle(R.string.bintu_title);
                    break;
                case 1:
                    toolbar.setTitle(R.string.rtmp_title);
                    break;
                case 2:
                    toolbar.setTitle(R.string.about_tab);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
