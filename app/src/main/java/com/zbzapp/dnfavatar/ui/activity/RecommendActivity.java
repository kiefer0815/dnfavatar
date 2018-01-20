package com.zbzapp.dnfavatar.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import butterknife.BindView;
import com.alibaba.fastjson.JSON;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.constant.Constants;
import com.zbzapp.dnfavatar.manager.PreferenceManager;
import com.zbzapp.dnfavatar.model.Recommend;
import com.zbzapp.dnfavatar.ui.adapter.RecommendAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kiefer on 2017/10/20.
 */

public class RecommendActivity extends  BackActivity {

        @BindView(R.id.recycler_view_content)
        RecyclerView mRecyclerView;
        private LinearLayoutManager mLayoutManager;
        private RecommendAdapter mResultAdapter;

        @Override
        protected int getLayoutRes() {
                return R.layout.activity_recommned;
        }

        public static void start(Context mContext) {
                Intent intent = new Intent(mContext, RecommendActivity.class);
                mContext.startActivity(intent);
        }

        @Override
        protected void initView() {
                super.initView();
                mLayoutManager = new LinearLayoutManager(this);
                mResultAdapter = new RecommendAdapter(this, new ArrayList<Recommend>());

                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.addItemDecoration(mResultAdapter.getItemDecoration());
                mRecyclerView.setAdapter(mResultAdapter);

        }

        @Override
        protected void initData() {
                String catStr = mPreference.getString(PreferenceManager.PREF_RECOMMEND_LIST, Constants.APP_CONFIG);
                List<Recommend> catList = null;
                try {
                        catList = JSON.parseArray(catStr,Recommend.class);
                }catch (Exception e){

                }
                if(catList!=null && catList.size()>0){
                        mResultAdapter.addAll(catList);
                        mResultAdapter.notifyDataSetChanged();
                }
        }

}
