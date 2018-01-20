package com.zbzapp.dnfavatar.ui.fragment.recyclerview.list;

/**
 * Created by kiefer on 2017/9/13.
 */

import android.content.Intent;
import android.support.annotation.ColorRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.BindView;
import butterknife.OnClick;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.manager.PreferenceManager;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.model.Chapter;
import com.zbzapp.dnfavatar.model.MiniComic;
import com.zbzapp.dnfavatar.ui.activity.ReaderActivity;
import com.zbzapp.dnfavatar.ui.adapter.BaseAdapter;
import com.zbzapp.dnfavatar.ui.adapter.ListAdapter;
import com.zbzapp.dnfavatar.ui.fragment.recyclerview.RecyclerViewFragment;
import com.zbzapp.dnfavatar.ui.view.ListView;
import com.zbzapp.dnfavatar.utils.HintUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/9/22.
 */

public abstract class ListFragment extends RecyclerViewFragment implements ListView {

        protected static final int DIALOG_REQUEST_OPERATION = 0;

        @BindView(R.id.grid_action_button)
        FloatingActionButton mActionButton;

        protected ListAdapter mListAdapter;

        protected long mSavedId = -1;

        @Override
        protected BaseAdapter initAdapter() {
                mListAdapter = new ListAdapter(getActivity(), new LinkedList<MiniComic>());
                mListAdapter.setTitleGetter(SourceManager.getInstance(this).new TitleGetter());
                mRecyclerView.setRecycledViewPool(getAppInstance().getGridRecycledPool());
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                switch (newState){
                                case RecyclerView.SCROLL_STATE_DRAGGING:
                                        getAppInstance().getBuilderProvider().pause();
                                        break;
                                case RecyclerView.SCROLL_STATE_IDLE:
                                        getAppInstance().getBuilderProvider().resume();
                                        break;
                                }
                        }
                });
                mActionButton.setImageResource(getActionButtonRes());
                return mListAdapter;
        }

        @Override
        protected RecyclerView.LayoutManager initLayoutManager() {
                LinearLayoutManager manager = new LinearLayoutManager(getActivity());
                manager.setRecycleChildrenOnDetach(true);
                return manager;
        }

        @OnClick(R.id.grid_action_button) void onActionButtonClick() {
                performActionButtonClick();
        }

        @Override
        public void onItemClick(View view, int position) {
                MiniComic comic = mListAdapter.getItem(position);
                startReader(comic);
        }

        private void startReader(MiniComic comic) {
                Chapter chapter =  new Chapter(comic.getTitle(),comic.getCover()); //ps这里特殊处理 cover装详情页地址
                List<Chapter> list = new ArrayList<Chapter>();
                list.add(chapter);
                int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
                Intent intent = ReaderActivity.createIntent(getActivity(), comic.getId(), list, mode);
                startActivity(intent);
        }

        @Override
        public void onItemLongClick(View view, int position) {

        }

        @Override
        public void onComicLoadSuccess(List<MiniComic> list) {
                mListAdapter.addAll(list);
        }

        @Override
        public void onComicLoadFail() {
                HintUtils.showToast(getActivity(), R.string.common_data_load_fail);
        }

        @Override
        public void onExecuteFail() {
                hideProgressDialog();
                HintUtils.showToast(getActivity(), R.string.common_execute_fail);
        }

        @Override
        public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
                mActionButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), accent));
        }


        protected abstract void performActionButtonClick();

        protected abstract int getActionButtonRes();

        protected abstract String[] getOperationItems();

        @Override
        protected int getLayoutRes() {
                return R.layout.fragment_list;
        }

}

