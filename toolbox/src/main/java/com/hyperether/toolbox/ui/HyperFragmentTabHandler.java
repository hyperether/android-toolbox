package com.hyperether.toolbox.ui;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

/**
 * HyperFragmentTabHandler - the main class for working with fragments
 *
 * @author Slobodan Prijic
 * @version 1.0 - 12/11/2017
 */
public abstract class HyperFragmentTabHandler {

    public static final String TAG = HyperFragmentTabHandler.class.getSimpleName();
    private AppCompatActivity activity;
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private LinearLayout tabbedLayout;
    private int numberOfTabs;

    // In this method inflate proper views, set number of fields etc.
    abstract void setPagerAdapterLayout();

    abstract Fragment getFragmentForPosition(int position);

    /**
     * The {@link PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link FragmentPagerAdapter} derivative, which will keep every loaded
     * fragment in memory. If this becomes too memory intensive, it may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    protected HyperFragmentTabHandler(AppCompatActivity appCompatActivity,
                                      TabLayout tabLayout,
                                      ViewPager viewPager,
                                      LinearLayout tabbedLayout,
                                      int numberOfTabs) {
        this.activity = appCompatActivity;
        this.mViewPager = viewPager;
        this.tabLayout = tabLayout;
        this.tabbedLayout = tabbedLayout;
        this.numberOfTabs = numberOfTabs;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                appCompatActivity.getSupportFragmentManager());
    }

    public void hideTabs() {
        if (tabbedLayout != null)
            tabbedLayout.setVisibility(View.GONE);
    }

    public void showTabs() {
        if (tabbedLayout != null)
            tabbedLayout.setVisibility(View.VISIBLE);
    }

    public void setPagerAdapter() {
        // Set up the ViewPager with the sections adapter.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCurrentItem(1);
        tabLayout.setupWithViewPager(mViewPager);

        setPagerAdapterLayout();

        // When RetrieveProvisioningActivity is showing up, this width parameter
        // is set to MATCH_PARENT from unknown reason, so we must set this parameter to WRAP_CONTENT
        // to be able for measure tabLayout width and compare it with device display width.
        final ViewGroup.LayoutParams mParams = tabLayout.getLayoutParams();
        final int lWidth = mParams.width;
        if (lWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
            mParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            tabLayout.setLayoutParams(mParams);
        }

        //expand tabs to fill parent on bigger devices
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                int tabLayoutWidth = tabLayout.getWidth();
                if (activity != null) {
                    int displayWidth = activity.getResources().getDisplayMetrics().widthPixels;

                    Log.d("21431243", "run: tabLayoutWidth:" + tabLayoutWidth + " displayWidth:"
                            + displayWidth + " lWidth" + lWidth);

                    if (tabLayoutWidth < displayWidth) {
                        tabLayout.setTabMode(TabLayout.MODE_FIXED);
                        mParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        tabLayout.setLayoutParams(mParams);
                    } else {
                        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                    }
                }
            }
        });
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
     * sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setFragmentTab(int i) {
            mViewPager.setCurrentItem(i);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return getFragmentForPosition(position);
        }

        @Override
        public int getCount() {
            return numberOfTabs;
        }

//        @Override
//        public CharSequence getPageTitle(int position) {
//            Locale l = Locale.getDefault();
//            switch (position) {
//                case 0:
//                    return getString(R.string.tab_history);
//                case 1:
//                    return getString(R.string.tab_dialer);
//                case 2:
//                    return getString(R.string.tab_contact);
//            }
//            return null;
//        }

    }
}