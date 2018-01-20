package com.zbzapp.dnfavatar.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.OnClick;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.constant.Constants;
import com.zbzapp.dnfavatar.fresco.ControllerBuilderSupplierFactory;
import com.zbzapp.dnfavatar.fresco.ImagePipelineFactoryBuilder;
import com.zbzapp.dnfavatar.global.Extra;
import com.zbzapp.dnfavatar.manager.PreferenceManager;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.model.Chapter;
import com.zbzapp.dnfavatar.model.Comic;
import com.zbzapp.dnfavatar.model.Task;
import com.zbzapp.dnfavatar.presenter.BasePresenter;
import com.zbzapp.dnfavatar.presenter.DetailPresenter;
import com.zbzapp.dnfavatar.service.DownloadService;
import com.zbzapp.dnfavatar.ui.adapter.BaseAdapter;
import com.zbzapp.dnfavatar.ui.adapter.DetailAdapter;
import com.zbzapp.dnfavatar.ui.view.DetailView;
import com.zbzapp.dnfavatar.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends CoordinatorActivity implements DetailView, NativeExpressAD.NativeExpressADListener {

    public static final int REQUEST_CODE_DOWNLOAD = 0;

    private DetailAdapter mDetailAdapter;
    private DetailPresenter mPresenter;
    private ImagePipelineFactory mImagePipelineFactory;

    private boolean mAutoBackup;
    private int mBackupCount;

    @BindView(R.id.container)
    FrameLayout container;
    private NativeExpressAD nativeExpressAD;
    private NativeExpressADView nativeExpressADView;
    public static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new DetailPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected BaseAdapter initAdapter() {
        mDetailAdapter = new DetailAdapter(this, new ArrayList<Chapter>());
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        return mDetailAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new GridLayoutManager(this, 4);
    }

    @Override
    protected void initData() {
        mAutoBackup = mPreference.getBoolean(PreferenceManager.PREF_BACKUP_SAVE_COMIC, true);
        mBackupCount = mPreference.getInt(PreferenceManager.PREF_BACKUP_SAVE_COMIC_COUNT, 0);
        long id = getIntent().getLongExtra(Extra.EXTRA_ID, -1);
        int source = getIntent().getIntExtra(Extra.EXTRA_SOURCE, -1);
        String cid = getIntent().getStringExtra(Extra.EXTRA_CID);
        mPresenter.load(id, source, cid);
        final float density = getResources().getDisplayMetrics().density;

        ADSize adSize = new ADSize((int) (getResources().getDisplayMetrics().widthPixels / density), 130); // 不支持MATCH_PARENT or WRAP_CONTENT，必须传入实际的宽高
        nativeExpressAD = new NativeExpressAD(DetailActivity.this, adSize, Constants.APPID, Constants.NativeExpressPosID3, this);
        nativeExpressAD.loadAD(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAutoBackup) {
            mPreference.putInt(PreferenceManager.PREF_BACKUP_SAVE_COMIC_COUNT, mBackupCount);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImagePipelineFactory != null) {
            mImagePipelineFactory.getImagePipeline().clearMemoryCaches();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
                case R.id.detail_history:
                    if (!mDetailAdapter.getDateSet().isEmpty()) {
                        String path = mPresenter.getComic().getLast();
                        if (path == null) {
                            path = mDetailAdapter.getItem(mDetailAdapter.getDateSet().size() - 1).getPath();
                        }
                        startReader(path);
                    }
                    break;
                case R.id.detail_download:
                    if (!mDetailAdapter.getDateSet().isEmpty()) {
                        intent = ChapterActivity.createIntent(this, new ArrayList<>(mDetailAdapter.getDateSet()));
                        startActivityForResult(intent, REQUEST_CODE_DOWNLOAD);
                    }
                    break;
                case R.id.detail_tag:
                    if (mPresenter.getComic().getFavorite() != null) {
                        intent = TagEditorActivity.createIntent(this, mPresenter.getComic().getId());
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.detail_tag_favorite);
                    }
                    break;
                case R.id.detail_search_title:
                    if (!StringUtils.isEmpty(mPresenter.getComic().getTitle())) {
                        intent = ResultActivity.createIntent(this, mPresenter.getComic().getTitle(), null, ResultActivity.LAUNCH_MODE_SEARCH);
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.common_keyword_empty);
                    }
                    break;
                case R.id.detail_search_author:
                    if (!StringUtils.isEmpty(mPresenter.getComic().getAuthor())) {
                        intent = ResultActivity.createIntent(this, mPresenter.getComic().getAuthor(), null, ResultActivity.LAUNCH_MODE_SEARCH);
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.common_keyword_empty);
                    }
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_DOWNLOAD:
                    showProgressDialog();
                    List<Chapter> list = data.getParcelableArrayListExtra(Extra.EXTRA_CHAPTER);
                    mPresenter.addTask(mDetailAdapter.getDateSet(), list);
                    break;
            }
        }
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        if (mPresenter.getComic().getFavorite() != null) {
            mPresenter.unfavoriteComic();
            increment();
            mActionButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            showSnackbar(R.string.detail_unfavorite);
        } else {
            mPresenter.favoriteComic();
            increment();
            mActionButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            showSnackbar(R.string.detail_favorite);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position != 0) {
            String path = mDetailAdapter.getItem(position - 1).getPath();
            startReader(path);
        }
    }

    private void startReader(String path) {
        long id = mPresenter.updateLast(path);
        mDetailAdapter.setLast(path);
        int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        Intent intent = ReaderActivity.createIntent(DetailActivity.this, id, mDetailAdapter.getDateSet(), mode);
        startActivity(intent);
    }

    @Override
    public void onLastChange(String last) {
        mDetailAdapter.setLast(last);
    }

    @Override
    public void onTaskAddSuccess(ArrayList<Task> list) {
        Intent intent = DownloadService.createIntent(this, list);
        startService(intent);
        updateChapterList(list);
        showSnackbar(R.string.detail_download_queue_success);
        hideProgressDialog();
    }

    private void updateChapterList(List<Task> list) {
        Set<String> set = new HashSet<>();
        for (Task task : list) {
            set.add(task.getPath());
        }
        for (Chapter chapter : mDetailAdapter.getDateSet()) {
            if (set.contains(chapter.getPath())) {
                chapter.setDownload(true);
            }
        }
    }

    @Override
    public void onTaskAddFail() {
        hideProgressDialog();
        showSnackbar(R.string.detail_download_queue_fail);
    }

    @Override
    public void onComicLoadSuccess(Comic comic) {
        mDetailAdapter.setInfo(comic.getCover(), comic.getTitle(), comic.getAuthor(),
                comic.getIntro(), comic.getFinish(), comic.getUpdate(), comic.getLast());

        if (comic.getTitle() != null && comic.getCover() != null) {
            mImagePipelineFactory = ImagePipelineFactoryBuilder.build(this, SourceManager.getInstance(this).getParser(comic.getSource()).getHeader(), false);
            mDetailAdapter.setControllerSupplier(ControllerBuilderSupplierFactory.get(this, mImagePipelineFactory));

            int resId = comic.getFavorite() != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
            mActionButton.setImageResource(resId);
            mActionButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChapterLoadSuccess(List<Chapter> list) {
        hideProgressBar();
        if (mPresenter.getComic().getTitle() != null && mPresenter.getComic().getCover() != null) {
            mDetailAdapter.addAll(list);
        }
    }

    @Override
    public void onParseError() {
        hideProgressBar();
        showSnackbar(R.string.common_parse_error);
    }

    private void increment() {
        if (mAutoBackup && ++mBackupCount == 10) {
            mBackupCount = 0;
            mPreference.putInt(PreferenceManager.PREF_BACKUP_SAVE_COMIC_COUNT, 0);
            mPresenter.backup();
        }
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.detail);
    }

    public static Intent createIntent(Context context, Long id, int source, String cid) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(Extra.EXTRA_ID, id);
        intent.putExtra(Extra.EXTRA_SOURCE, source);
        intent.putExtra(Extra.EXTRA_CID, cid);
        return intent;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //广告相关
    @Override
    public void onNoAD(AdError adError) {
        Log.i(
                TAG,
                String.format("onNoAD, error code: %d, error msg: %s", adError.getErrorCode(),
                        adError.getErrorMsg()));
    }

    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {
        Log.i(TAG, "onADLoaded: " + adList.size());
        // 释放前一个NativeExpressADView的资源
        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }

        if (container.getVisibility() != View.VISIBLE) {
            container.setVisibility(View.VISIBLE);
        }

        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }

        nativeExpressADView = adList.get(0);
        // 保证View被绘制的时候是可见的，否则将无法产生曝光和收益。
        container.addView(nativeExpressADView);
        nativeExpressADView.render();
    }

    @Override
    public void onRenderFail(NativeExpressADView adView) {
        Log.i(TAG, "onRenderFail");
    }

    @Override
    public void onRenderSuccess(NativeExpressADView adView) {
        Log.i(TAG, "onRenderSuccess");
    }

    @Override
    public void onADExposure(NativeExpressADView adView) {
        Log.i(TAG, "onADExposure");
    }

    @Override
    public void onADClicked(NativeExpressADView adView) {
        Log.i(TAG, "onADClicked");
    }

    @Override
    public void onADClosed(NativeExpressADView adView) {
        Log.i(TAG, "onADClosed");
        // 当广告模板中的关闭按钮被点击时，广告将不再展示。NativeExpressADView也会被Destroy，不再可用。
        if (container != null && container.getChildCount() > 0) {
            container.removeAllViews();
            container.setVisibility(View.GONE);
        }
    }

    @Override
    public void onADLeftApplication(NativeExpressADView adView) {
        Log.i(TAG, "onADLeftApplication");
    }

    @Override
    public void onADOpenOverlay(NativeExpressADView adView) {
        Log.i(TAG, "onADOpenOverlay");
    }

}
