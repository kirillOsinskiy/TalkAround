package com.osk.talkaround.client.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.osk.talkaround.client.activities.OnDataUpdate;
import com.osk.talkaround.client.activities.UpdatableFragment;
import com.osk.talkaround.model.Talk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GZaripov1 on 11.03.2017.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter implements OnDataUpdate {
    private final List<UpdatableFragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    public void addFragment(UpdatableFragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public void updateTalks(Talk[] talks) {
        for (UpdatableFragment updatableFragment : mFragmentList) {
            updatableFragment.updateTalks(talks);
        }
    }
}