package org.cirrus.mobi.pegel.md;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.cirrus.mobi.pegel.R;

public class PegelDetailsTabsAdapter extends FragmentStatePagerAdapter {

    private static final int TAB_COUNT = 4;
    private final String mpoint;
    private final String river;
    private final String pnr;
    private final Resources mResources;

    public PegelDetailsTabsAdapter(FragmentManager fm, String pnr, String river, String mpoint, Resources resources) {
        super(fm);
        this.pnr = pnr;
        this.river = river;
        this.mpoint = mpoint;
        this.mResources = resources;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return PegelDataFragment.getNewInstance(pnr, river, mpoint);
            case 1:
                return PegelDataDetailFragment.getNewInstance(pnr);
            case 2:
                return PegelDetailFragment.getNewInstance(pnr);
            case 3:
                return PegelMapFragment.newInstance(pnr);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mResources.getString(R.string.tab1);
            case 1:
                return mResources.getString(R.string.tab2);
            case 2:
                return mResources.getString(R.string.tab4);
            case 3:
                return mResources.getString(R.string.tab3);
        }
        return null;
    }

    @Override
    public int getCount() {
        return TAB_COUNT;
    }
}