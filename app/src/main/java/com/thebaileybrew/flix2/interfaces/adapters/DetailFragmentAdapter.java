package com.thebaileybrew.flix2.interfaces.adapters;

import android.content.Context;
import android.os.Bundle;

import com.thebaileybrew.flix2.fragments.OverviewFragment;
import com.thebaileybrew.flix2.models.Movie;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class DetailFragmentAdapter extends FragmentPagerAdapter{

    private Context context;

    public DetailFragmentAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: //Returns Overview Fragment
                return new OverviewFragment();
            case 1: //Returns Reviews Fragment
                return new OverviewFragment();
            case 2: //Returns Video Fragment
                return new OverviewFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0: //Returns Overview Title for Fragment
                return "OVERVIEW";
            case 1: //Returns Reviews Title for Fragment
                return "REVIEWS";
            case 2: //Returns Video Title for Fragment
                return "VIDEOS";
            default:
                return null;
        }
    }
}
