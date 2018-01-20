package com.zbzapp.dnfavatar.ui.activity;

import android.content.Context;
import android.content.Intent;

import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.global.Extra;
import com.zbzapp.dnfavatar.presenter.BasePresenter;
import com.zbzapp.dnfavatar.presenter.SourceDetailPresenter;
import com.zbzapp.dnfavatar.ui.view.SourceDetailView;
import com.zbzapp.dnfavatar.ui.widget.Option;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public class SourceDetailActivity extends BackActivity implements SourceDetailView {

    private SourceDetailPresenter mPresenter;

    @BindView(R.id.source_detail_type) Option mSourceType;
    @BindView(R.id.source_detail_title) Option mSourceTitle;
    @BindView(R.id.source_detail_favorite) Option mSourceFavorite;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new SourceDetailPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initData() {
        mPresenter.load(getIntent().getIntExtra(Extra.EXTRA_SOURCE, -1));
    }

    @OnClick(R.id.source_detail_favorite) void onSourceFavoriteClick() {
        // TODO 显示这个图源的漫画
    }

    @Override
    public void onSourceLoadSuccess(int type, String title, long count) {
        mSourceType.setSummary(String.valueOf(type));
        mSourceTitle.setSummary(title);
        mSourceFavorite.setSummary(String.valueOf(count));
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_source_detail;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.source_detail);
    }

    public static Intent createIntent(Context context, int type) {
        Intent intent = new Intent(context, SourceDetailActivity.class);
        intent.putExtra(Extra.EXTRA_SOURCE, type);
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
