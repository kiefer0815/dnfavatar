package com.zbzapp.dnfavatar.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by jingbin on 2016/12/6.
 */

public class DynamicFragmentPagerAdapter extends FragmentStatePagerAdapter {
        private int length ;

        private ArrayList<Fragment> mFragments;//碎片数组
        private ArrayList<String> mTitles;//碎片数组
        private int mCount;
        FragmentManager fm ;
        public DynamicFragmentPagerAdapter(FragmentManager fm , ArrayList<Fragment> mFragments,ArrayList<String> titles) {
                super(fm);
                this.fm = fm;
                this.length = mFragments.size();
                this.mFragments = mFragments;
                this.mTitles = titles;
                FragmentTransaction mTransaction = fm.beginTransaction();
        }

        @Override
        public int getItemPosition(Object object) {
                return POSITION_NONE; //这个是必须的
        }

        @Override
        public Fragment getItem(int position) {
                int size = mFragments.size();
                if(position >= size)
                        return mFragments.get(size - 1);
                return mFragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
                return mTitles.get(position);
        }

        @Override
        public int getCount() {
                if(mCount > 0) {
                        return mCount;
                } else {
                        return length;
                }
        }

        public void setFragments(ArrayList<Fragment> fragments,ArrayList<String> tags) {
                if(this.mFragments != null){
                        FragmentTransaction ft = fm.beginTransaction();
                        for(Fragment f:this.mFragments){
                                ft.remove(f);
                        }
                        ft.commit();
                        ft=null;
                        fm.executePendingTransactions();
                }
                this.mFragments.clear();
                this.mFragments.addAll(fragments);
                this.mTitles.clear();
                this.mTitles.addAll(tags);
                notifyDataSetChanged();
        }

        public void setMaxCount(int count) {
                if(mCount < length) {
                        if(mCount != count) {
                                mCount = count;
                                notifyDataSetChanged();
                        }
                }
        }

        public void setmFragments(ArrayList<Fragment> mFragments) {
                this.mFragments = mFragments;
        }
}