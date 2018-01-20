package com.zbzapp.dnfavatar.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.BindView;
import com.zbzapp.dnfavatar.App;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.global.Extra;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.manager.TagManager;
import com.zbzapp.dnfavatar.model.MiniComic;
import com.zbzapp.dnfavatar.presenter.BasePresenter;
import com.zbzapp.dnfavatar.presenter.PartFavoritePresenter;
import com.zbzapp.dnfavatar.ui.adapter.BaseAdapter;
import com.zbzapp.dnfavatar.ui.adapter.GridAdapter;
import com.zbzapp.dnfavatar.ui.fragment.dialog.MessageDialogFragment;
import com.zbzapp.dnfavatar.ui.fragment.dialog.MultiDialogFragment;
import com.zbzapp.dnfavatar.ui.view.PartFavoriteView;
import com.zbzapp.dnfavatar.utils.HintUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class PartFavoriteActivity extends BackActivity implements PartFavoriteView, BaseAdapter.OnItemClickListener,
        BaseAdapter.OnItemLongClickListener {

    private static final int DIALOG_REQUEST_DELETE = 0;
    private static final int DIALOG_REQUEST_ADD = 1;

    @BindView(R.id.part_favorite_recycler_view) RecyclerView mRecyclerView;

    private PartFavoritePresenter mPresenter;
    private GridAdapter mGridAdapter;

    private MiniComic mSavedComic;
    private boolean isDeletable;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new PartFavoritePresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
        mGridAdapter = new GridAdapter(this, new LinkedList<Object>());
        mGridAdapter.setSymbol(true);
        mGridAdapter.setProvider(((App) getApplication()).getBuilderProvider());
        mGridAdapter.setTitleGetter(SourceManager.getInstance(this).new TitleGetter());
        mGridAdapter.setOnItemClickListener(this);
        mGridAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.addItemDecoration(mGridAdapter.getItemDecoration());
        mRecyclerView.setAdapter(mGridAdapter);
    }

    @Override
    protected void initData() {
        long id = getIntent().getLongExtra(Extra.EXTRA_ID, -1);
        isDeletable = id != TagManager.TAG_CONTINUE && id != TagManager.TAG_FINISH;
        mPresenter.load(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getLongExtra(Extra.EXTRA_ID, -1) >= 0) {
            getMenuInflater().inflate(R.menu.menu_part_favorite, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.part_favorite_add:
                showProgressDialog();
                List<MiniComic> tmp =  new ArrayList<MiniComic>();
                for (Object comic :mGridAdapter.getDateSet()){
                    if(comic instanceof MiniComic){
                        MiniComic miniComic = (MiniComic)comic;
                        tmp.add(miniComic);
                    }
                }
                mPresenter.loadComicTitle(tmp);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        if(mGridAdapter.getItem(position) instanceof MiniComic) {
            MiniComic comic = (MiniComic) mGridAdapter.getItem(position);
            Intent intent = DetailActivity.createIntent(this, comic.getId(), -1, null);
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        if (isDeletable) {
            if(mGridAdapter.getItem(position) instanceof MiniComic) {
                mSavedComic = (MiniComic) mGridAdapter.getItem(position);
                MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                    R.string.part_favorite_delete_confirm, true, DIALOG_REQUEST_DELETE);
                fragment.show(getFragmentManager(), null);
            }
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_DELETE:
                long id = mSavedComic.getId();
                mPresenter.delete(id);
                mGridAdapter.remove(mSavedComic);
                HintUtils.showToast(this, R.string.common_execute_success);
                break;
            case DIALOG_REQUEST_ADD:
                showProgressDialog();
                boolean[] check = bundle.getBooleanArray(EXTRA_DIALOG_RESULT_VALUE);
                mPresenter.insert(check);
                break;
        }
    }

    @Override
    public void onComicLoadFail() {
        hideProgressBar();
        HintUtils.showToast(this, R.string.common_data_load_fail);
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        hideProgressBar();
        List<Object> tmp =  new ArrayList<Object>();
        for (MiniComic comic :list){
            tmp.add(comic);
        }
        mGridAdapter.addAll(tmp);
    }

    @Override
    public void onComicTitleLoadSuccess(List<String> list) {
        hideProgressDialog();
        MultiDialogFragment fragment = MultiDialogFragment.newInstance(R.string.part_favorite_select,
                list.toArray(new String[list.size()]), null, DIALOG_REQUEST_ADD);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onComicTitleLoadFail() {
        hideProgressDialog();
        HintUtils.showToast(this, R.string.common_data_load_fail);
    }

    @Override
    public void onComicInsertSuccess(List<MiniComic> list) {
        hideProgressDialog();
        List<Object> tmp =  new ArrayList<Object>();
        for (MiniComic comic :list){
            tmp.add(comic);
        }
        mGridAdapter.addAll(tmp);
        HintUtils.showToast(this, R.string.common_execute_success);
    }

    @Override
    public void onComicInsertFail() {
        hideProgressDialog();
        HintUtils.showToast(this, R.string.common_execute_fail);
    }

    @Override
    public void onHighlightCancel(MiniComic comic) {
        mGridAdapter.moveItemTop(comic);
    }

    @Override
    public void onComicRead(MiniComic comic) {
        mGridAdapter.moveItemTop(comic);
    }

    @Override
    public void onComicRemove(long id) {
        mGridAdapter.removeItemById(id);
    }

    @Override
    public void onComicAdd(MiniComic comic) {
        if (!mGridAdapter.contains(comic)) {
            mGridAdapter.add(0, comic);
        }
    }

    @Override
    protected String getDefaultTitle() {
        return getIntent().getStringExtra(Extra.EXTRA_KEYWORD);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_part_favorite;
    }

    @Override
    protected boolean isNavTranslation() {
        return true;
    }

    public static Intent createIntent(Context context, long id, String title) {
        Intent intent = new Intent(context, PartFavoriteActivity.class);
        intent.putExtra(Extra.EXTRA_ID, id);
        intent.putExtra(Extra.EXTRA_KEYWORD, title);
        return intent;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
