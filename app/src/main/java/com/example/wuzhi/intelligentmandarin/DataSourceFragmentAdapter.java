package com.example.wuzhi.intelligentmandarin;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.wuzhi.intelligentmandarin.DataClass.ExerciseDataSource;

import java.util.List;

/**
 * Created by wuzhi on 2017/5/5.
 */

public class DataSourceFragmentAdapter extends FragmentStatePagerAdapter {
    private List<DataSourceFragment> fragments;
    private List<ExerciseDataSource> dataSources;

    public DataSourceFragmentAdapter(FragmentManager fm, List<DataSourceFragment> fragments, List<ExerciseDataSource> dataSources) {
        super(fm);
        this.fragments = fragments;
        this.dataSources = dataSources;
    }

    @Override
    public Fragment getItem(int position) {
        DataSourceFragment dataSourceFragment = fragments.get(position);
        dataSourceFragment.setExerciseData(dataSources.get(position));
        return dataSourceFragment;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
