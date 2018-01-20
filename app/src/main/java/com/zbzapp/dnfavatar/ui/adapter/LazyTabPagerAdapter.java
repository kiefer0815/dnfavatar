package com.zbzapp.dnfavatar.ui.adapter;

/**
 * Created by kiefer on 2017/9/12.
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import com.zbzapp.dnfavatar.ui.fragment.LazyBaseFragment;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class LazyTabPagerAdapter extends FragmentPagerAdapter {

        private LazyBaseFragment[] fragment;
        private String[] title;

        public LazyTabPagerAdapter(FragmentManager manager, LazyBaseFragment[] fragment, String[] title) {
                super(manager);
                this.fragment = fragment;
                this.title = title;
        }

        @Override
        public Fragment getItem(int position) {
                return fragment[position];
        }

        @Override
        public int getCount() {
                return fragment.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
                return title[position];
        }

}
