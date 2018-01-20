package com.zbzapp.dnfavatar.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.facebook.binaryresource.BinaryResource;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.github.clans.fab.FloatingActionButton;
import com.qq.e.ads.banner.ADSize;
import com.qq.e.ads.banner.AbstractBannerADListener;
import com.qq.e.ads.banner.BannerView;
import com.qq.e.comm.util.AdError;
import com.zbzapp.dnfavatar.App;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.constant.Constants;
import com.zbzapp.dnfavatar.fresco.ControllerBuilderSupplierFactory;
import com.zbzapp.dnfavatar.fresco.ImagePipelineFactoryBuilder;
import com.zbzapp.dnfavatar.global.ClickEvents;
import com.zbzapp.dnfavatar.global.Extra;
import com.zbzapp.dnfavatar.manager.PreferenceManager;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.model.Chapter;
import com.zbzapp.dnfavatar.model.ImageUrl;
import com.zbzapp.dnfavatar.presenter.BasePresenter;
import com.zbzapp.dnfavatar.presenter.ReaderPresenter;
import com.zbzapp.dnfavatar.ui.adapter.ReaderAdapter;
import com.zbzapp.dnfavatar.ui.adapter.ReaderAdapter.OnLazyLoadListener;
import com.zbzapp.dnfavatar.ui.view.ReaderView;
import com.zbzapp.dnfavatar.ui.widget.OnTapGestureListener;
import com.zbzapp.dnfavatar.ui.widget.PreCacheLayoutManager;
import com.zbzapp.dnfavatar.ui.widget.RetryDraweeView;
import com.zbzapp.dnfavatar.ui.widget.ReverseSeekBar;
import com.zbzapp.dnfavatar.utils.HintUtils;
import com.zbzapp.dnfavatar.utils.StringUtils;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/8/6.
 */
public abstract class ReaderActivity extends BaseActivity implements OnTapGestureListener,
        OnProgressChangeListener, OnLazyLoadListener, ReaderView {

    @BindView(R.id.reader_chapter_title) TextView mChapterTitle;
    @BindView(R.id.reader_chapter_page) TextView mChapterPage;
    @BindView(R.id.reader_battery) TextView mBatteryText;
    @BindView(R.id.reader_progress_layout) View mProgressLayout;
    @BindView(R.id.reader_back_layout) View mBackLayout;
    @BindView(R.id.reader_info_layout) View mInfoLayout;
    @BindView(R.id.reader_seek_bar) ReverseSeekBar mSeekBar;
    @BindView(R.id.reader_loading) TextView mLoadingText;
    @BindView(R.id.float_fav)
    FloatingActionButton mFloatFav;
    @BindView(R.id.float_download)
    FloatingActionButton mFloatDownload;
    @BindView(R.id.float_replay)
    FloatingActionButton mFloatReplay;

    @BindView(R.id.reader_recycler_view) RecyclerView mRecyclerView;

    BannerView bv;
    @BindView(R.id.rl_read)
    RelativeLayout rl_read;

    protected PreCacheLayoutManager mLayoutManager;
    protected ReaderAdapter mReaderAdapter;
    protected ImagePipelineFactory mImagePipelineFactory;
    protected ImagePipelineFactory mLargeImagePipelineFactory;

    protected ReaderPresenter mPresenter;
    protected int mLastDx = 0;
    protected int mLastDy = 0;
    protected int progress = 1;
    protected int max = 1;
    protected int turn;
    protected int orientation;
    protected int mode;

    protected boolean mLoadPrev;
    protected boolean mLoadNext;

    private boolean isSavingPicture = false;

    private boolean mHideInfo;
    private boolean mHideNav;
    private int[] mClickArray;
    private int[] mLongClickArray;

    @Override
    protected void initTheme() {
        super.initTheme();
        mHideNav = mPreference.getBoolean(PreferenceManager.PREF_READER_HIDE_NAV, false);
        if (!mHideNav || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (mPreference.getBoolean(PreferenceManager.PREF_READER_KEEP_BRIGHT, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        mode = getIntent().getIntExtra(Extra.EXTRA_MODE, PreferenceManager.READER_MODE_PAGE);
        String key = mode == PreferenceManager.READER_MODE_PAGE ?
                PreferenceManager.PREF_READER_PAGE_ORIENTATION : PreferenceManager.PREF_READER_STREAM_ORIENTATION;
        orientation = mPreference.getInt(key, PreferenceManager.READER_ORIENTATION_PORTRAIT);
        setRequestedOrientation(orientation == PreferenceManager.READER_ORIENTATION_PORTRAIT ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new ReaderPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        mHideInfo = mPreference.getBoolean(PreferenceManager.PREF_READER_HIDE_INFO, false);
        mInfoLayout.setVisibility(mHideInfo ? View.INVISIBLE : View.VISIBLE);
        String key = mode == PreferenceManager.READER_MODE_PAGE ?
                PreferenceManager.PREF_READER_PAGE_TURN : PreferenceManager.PREF_READER_STREAM_TURN;
        turn = mPreference.getInt(key, PreferenceManager.READER_TURN_LTR);
        initSeekBar();
        initLayoutManager();
        initReaderAdapter();
        initFloatButton();
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mReaderAdapter);
        mRecyclerView.setItemViewCacheSize(2);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mLastDx = dx;
                mLastDy = dy;
            }
        });

    }

    private void initFloatButton() {

        mFloatDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doClickEvent(ClickEvents.EVENT_SAVE_PICTURE);
            }
        });

        mFloatFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPresenter.getComic().getFavorite() != null) {
                    mPresenter.unfavoriteComic();
                    mFloatFav.setImageResource(R.drawable.ic_favorite_border_white_24dp);
                    HintUtils.showToast(ReaderActivity.this,R.string.favorite_delete);
                } else {
                    mPresenter.favoriteComic();
                    mFloatFav.setImageResource(R.drawable.ic_favorite_white_24dp);
                    HintUtils.showToast(ReaderActivity.this,R.string.favorite);
                    showSnackbar(R.string.detail_favorite);
                }
            }
        });
        mFloatReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadImage();
            }
        });
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (mHideNav) {
            int options = getWindow().getDecorView().getSystemUiVisibility();

            options |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                options |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                options |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            getWindow().getDecorView().setSystemUiVisibility(options);
        }
    }

    private void initSeekBar() {
        mSeekBar.setReverse(turn == PreferenceManager.READER_TURN_RTL);
        mSeekBar.setOnProgressChangeListener(this);
    }

    private void initReaderAdapter() {
        mReaderAdapter = new ReaderAdapter(this, new LinkedList<ImageUrl>());
        mReaderAdapter.setTapGestureListener(this);
        mReaderAdapter.setLazyLoadListener(this);
        mReaderAdapter.setScaleFactor(mPreference.getInt(PreferenceManager.PREF_READER_SCALE_FACTOR, 200) * 0.01f);
        mReaderAdapter.setDoubleTap(!mPreference.getBoolean(PreferenceManager.PREF_READER_BAN_DOUBLE_CLICK, false));
        mReaderAdapter.setVertical(turn == PreferenceManager.READER_TURN_ATB);
        mReaderAdapter.setPaging(mPreference.getBoolean(PreferenceManager.PREF_READER_PAGING, false));
        mReaderAdapter.setPortrait(orientation == PreferenceManager.READER_ORIENTATION_PORTRAIT);
        mReaderAdapter.setWhiteEdge(mPreference.getBoolean(PreferenceManager.PREF_READER_WHITE_EDGE, false));
        mReaderAdapter.setBanTurn(mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_BAN_TURN, false));
    }

    private void initLayoutManager() {
        mLayoutManager = new PreCacheLayoutManager(this);
        mLayoutManager.setOrientation(turn == PreferenceManager.READER_TURN_ATB ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL);
        mLayoutManager.setReverseLayout(turn == PreferenceManager.READER_TURN_RTL);
        mLayoutManager.setExtraSpace(2);
    }

    @Override
    protected void initData() {
        mClickArray = mode == PreferenceManager.READER_MODE_PAGE ?
                ClickEvents.getPageClickEventChoice(mPreference) : ClickEvents.getStreamClickEventChoice(mPreference);
        mLongClickArray = mode == PreferenceManager.READER_MODE_PAGE ?
                ClickEvents.getPageLongClickEventChoice(mPreference) : ClickEvents.getStreamLongClickEventChoice(mPreference);
        long id = getIntent().getLongExtra(Extra.EXTRA_ID, -1);
        List<Chapter> list = getIntent().getParcelableArrayListExtra(Extra.EXTRA_CHAPTER);
        mPresenter.loadInit(id, list.toArray(new Chapter[list.size()]));
        int resId = mPresenter.getComic().getFavorite() != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
        mFloatFav.setImageResource(resId);
        mFloatFav.setVisibility(View.VISIBLE);
        initAd();
        bv.loadAD();
    }

    private void initAd(){
        bv = new BannerView(this, ADSize.BANNER, Constants.APPID, Constants.BannerPosID);
        // 注意：如果开发者的banner不是始终展示在屏幕中的话，请关闭自动刷新，否则将导致曝光率过低。
        // 并且应该自行处理：当banner广告区域出现在屏幕后，再手动loadAD。
        bv.setRefresh(30);
        bv.setShowClose(true);
        bv.setADListener(new AbstractBannerADListener() {

            @Override
            public void onNoAD(AdError adError) {
                Log.i("AD_DEMO", "BannerNoAD，eCode=" + adError.getErrorMsg());
            }

            @Override
            public void onADReceiv() {
                Log.i("AD_DEMO", "ONBannerReceive");
            }
        });
        rl_read.addView(bv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPresenter != null) {
            mPresenter.updateComic(progress);
        }
        unregisterReceiver(batteryReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImagePipelineFactory != null) {
            mImagePipelineFactory.getImagePipeline().clearMemoryCaches();
        }
        if (mLargeImagePipelineFactory != null) {
            mLargeImagePipelineFactory.getImagePipeline().clearMemoryCaches();
        }
    }

    @OnClick(R.id.reader_back_btn) void onBackClick() {
        onBackPressed();
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {}

    @Override
    public void onLoad(ImageUrl imageUrl) {
        mPresenter.lazyLoad(imageUrl);
    }

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                String text = (level * 100 / scale) + "%";
                mBatteryText.setText(text);
            }
        }
    };

    protected void hideControl() {
        if (mProgressLayout.isShown()) {
            Animation upAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, -1.0f);
            upAction.setDuration(300);
            Animation downAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 1.0f);
            downAction.setDuration(300);
            mProgressLayout.startAnimation(downAction);
            mProgressLayout.setVisibility(View.INVISIBLE);
            mBackLayout.startAnimation(upAction);
            mBackLayout.setVisibility(View.INVISIBLE);
            if (mHideInfo) {
                mInfoLayout.startAnimation(upAction);
                mInfoLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    protected void showControl() {
        Animation upAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        upAction.setDuration(300);
        Animation downAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        downAction.setDuration(300);
        if (mSeekBar.getMax() != max) {
            mSeekBar.setMax(max);
            mSeekBar.setProgress(max);
        }
        mSeekBar.setProgress(progress);
        mProgressLayout.startAnimation(upAction);
        mProgressLayout.setVisibility(View.VISIBLE);
        mBackLayout.startAnimation(downAction);
        mBackLayout.setVisibility(View.VISIBLE);
        if (mHideInfo) {
            mInfoLayout.startAnimation(downAction);
            mInfoLayout.setVisibility(View.VISIBLE);
        }
    }

    protected void updateProgress() {
        mChapterPage.setText(StringUtils.getProgress(progress, max));
    }

    @Override
    public void onPicturePaging(ImageUrl image) {
        int pos = mReaderAdapter.getPositionById(image.getId());
        mReaderAdapter.add(pos + 1, new ImageUrl(image.getNum(), image.getUrls(),
                image.getChapter(), ImageUrl.STATE_PAGE_2, false));
    }

    @Override
    public void onParseError() {
        HintUtils.showToast(this, R.string.common_parse_error);
    }

    @Override
    public void onNextLoadSuccess(List<ImageUrl> list) {
        mReaderAdapter.addAll(list);
        HintUtils.showToast(this, R.string.reader_load_success);
    }

    @Override
    public void onPrevLoadSuccess(List<ImageUrl> list) {
        mReaderAdapter.addAll(0, list);
        HintUtils.showToast(this, R.string.reader_load_success);
    }

    @Override
    public void onInitLoadSuccess(List<ImageUrl> list, int progress, int source, boolean local) {
        mImagePipelineFactory = ImagePipelineFactoryBuilder
                .build(this, local ? null : SourceManager.getInstance(this).getParser(source).getHeader(), false);
        mLargeImagePipelineFactory = ImagePipelineFactoryBuilder
                .build(this, local ? null : SourceManager.getInstance(this).getParser(source).getHeader(), true);
        mReaderAdapter.setControllerSupplier(ControllerBuilderSupplierFactory.get(this, mImagePipelineFactory),
                ControllerBuilderSupplierFactory.get(this, mLargeImagePipelineFactory));
        mReaderAdapter.addAll(list);
        if (progress != 1) {
            mRecyclerView.scrollToPosition(progress - 1);
        }
        mLoadingText.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        updateProgress();
    }

    @Override
    public void onChapterChange(Chapter chapter) {
        max = chapter.getCount();
        mChapterTitle.setText(chapter.getTitle());
    }

    @Override
    public void onImageLoadSuccess(int id, String url) {
        mReaderAdapter.update(id, url);
    }

    @Override
    public void onImageLoadFail(int id) {
        mReaderAdapter.update(id, null);
    }

    @Override
    public void onPictureSaveSuccess(Uri uri) {
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        isSavingPicture = false;
        HintUtils.showToast(this, R.string.reader_picture_save_success);
    }

    @Override
    public void onPictureSaveFail() {
        isSavingPicture = false;
        HintUtils.showToast(this, R.string.reader_picture_save_fail);
    }

    @Override
    public void onPrevLoading() {
        HintUtils.showToast(this, R.string.reader_load_prev);
    }

    @Override
    public void onPrevLoadNone() {
        HintUtils.showToast(this, R.string.reader_prev_none);
    }

    @Override
    public void onNextLoading() {
        HintUtils.showToast(this, R.string.reader_load_next);
    }

    @Override
    public void onNextLoadNone() {
        HintUtils.showToast(this, R.string.reader_next_none);
    }

    /**
     *  Click Event Function
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mReaderAdapter.getItemCount() != 0) {
            int value = ClickEvents.EVENT_NULL;
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    value = mClickArray[5];
                    break;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    value = mClickArray[6];
                    break;
            }
            if (value != ClickEvents.EVENT_NULL) {
                doClickEvent(value);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSingleTap(float x, float y) {
        doClickEvent(getValue(x, y, false));
    }

    @Override
    public void onLongPress(float x, float y) {
        doClickEvent(getValue(x, y, true));
    }

    private int getValue(float x, float y, boolean isLong) {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        int position = getCurPosition();
        if (position == -1) {
            position = mLayoutManager.findFirstVisibleItemPosition();
        }
        RetryDraweeView draweeView = ((ReaderAdapter.ImageHolder)
                mRecyclerView.findViewHolderForAdapterPosition(position)).draweeView;
        float limitX = point.x / 3.0f;
        float limitY = point.y / 3.0f;
        if (x < limitX) {
            return isLong ? mLongClickArray[0] : mClickArray[0];
        } else if (x > 2 * limitX) {
            return isLong ? mLongClickArray[4] : mClickArray[4];
        } else if (y < limitY) {
            return isLong ? mLongClickArray[1] : mClickArray[1];
        } else if (y > 2 * limitY) {
            return isLong ? mLongClickArray[3] : mClickArray[3];
        } else if (!draweeView.retry()) {
            return isLong ? mLongClickArray[2] : mClickArray[2];
        }
        return 0;
    }

    private void doClickEvent(int value) {
        switch (value) {
            case ClickEvents.EVENT_PREV_PAGE:
                prevPage();
                break;
            case ClickEvents.EVENT_NEXT_PAGE:
                nextPage();
                break;
            case ClickEvents.EVENT_SAVE_PICTURE:
                savePicture();
                break;
            case ClickEvents.EVENT_LOAD_PREV:
                loadPrev();
                break;
            case ClickEvents.EVENT_LOAD_NEXT:
                loadNext();
                break;
            case ClickEvents.EVENT_EXIT_READER:
                exitReader();
                break;
            case ClickEvents.EVENT_TO_FIRST:
                toFirst();
                break;
            case ClickEvents.EVENT_TO_LAST:
                toLast();
                break;
            case ClickEvents.EVENT_SWITCH_SCREEN:
                switchScreen();
                break;
            case ClickEvents.EVENT_SWITCH_MODE:
                switchMode();
                break;
            case ClickEvents.EVENT_SWITCH_CONTROL:
                switchControl();
                break;
            case ClickEvents.EVENT_RELOAD_IMAGE:
                reloadImage();
                break;
            case ClickEvents.EVENT_SWITCH_NIGHT:
                switchNight();
                break;
        }
    }

    protected abstract int getCurPosition();

    protected abstract void prevPage();

    protected abstract void nextPage();

    protected void switchNight() {
        boolean night = !mPreference.getBoolean(PreferenceManager.PREF_NIGHT, false);
        mPreference.putBoolean(PreferenceManager.PREF_NIGHT, night);
        if (mNightMask != null) {
            mNightMask.setVisibility(night ? View.VISIBLE : View.INVISIBLE);
        }
        mPresenter.switchNight();
    }

    protected void reloadImage() {
        int position = getCurPosition();
        if (position == -1) {
            position = mLayoutManager.findFirstVisibleItemPosition();
        }
        if(mReaderAdapter.getItemCount() > position){
            ImageUrl image = mReaderAdapter.getItem(position);
            H5Activity.start(this,image.getUrl());
        }

//        String rawUrl = image.getUrl();
//        String postUrl = StringUtils.format("%s-post-%d", image.getUrl(), image.getId());
//        ImagePipelineFactory factory = image.getSize() > App.mLargePixels ?
//                mLargeImagePipelineFactory : mImagePipelineFactory;
//        factory.getImagePipeline().evictFromCache(Uri.parse(rawUrl));
//        factory.getImagePipeline().evictFromCache(Uri.parse(postUrl));
//        mReaderAdapter.notifyItemChanged(position);
    }

    protected void savePicture() {
        if (isSavingPicture || mReaderAdapter==null) {
            return;
        }
        isSavingPicture = true;

        int position = getCurPosition();
        if (position == -1 || position >= mReaderAdapter.getItemCount()) {
            position = mLayoutManager.findFirstVisibleItemPosition();
        }
        if(mReaderAdapter.getItemCount() <= position) return;
        ImageUrl imageUrl = mReaderAdapter.getItem(position);
        String[] urls = imageUrl.getUrls();
        try {
            String title = mChapterTitle.getText().toString();
            for (String url : urls) {
                if (url.startsWith("file")) {
                    mPresenter.savePicture(new FileInputStream(new File(Uri.parse(url).getPath())), url, title, progress);
                    return;
                } else if (url.startsWith("content")) {
                    mPresenter.savePicture(getContentResolver().openInputStream(Uri.parse(url)), url, title, progress);
                    return;
                } else {
                    ImagePipelineFactory factory = imageUrl.getSize() > App.mLargePixels ?
                            mLargeImagePipelineFactory : mImagePipelineFactory;
                    BinaryResource resource = factory.getMainFileCache().getResource(new SimpleCacheKey(url));
                    if (resource != null) {
                        mPresenter.savePicture(resource.openStream(), url, title, progress);
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        onPictureSaveFail();
    }

    protected void loadPrev() {
        mPresenter.loadPrev();
    }

    protected void loadNext() {
        mPresenter.loadNext();
    }

    protected void exitReader() {
        finish();
    }

    protected void toFirst() {
        mRecyclerView.scrollToPosition(0);
    }

    protected void toLast() {
        mRecyclerView.scrollToPosition(mReaderAdapter.getItemCount() - 1);
    }

    protected void switchScreen() {
        if (orientation == PreferenceManager.READER_ORIENTATION_PORTRAIT) {
            orientation = PreferenceManager.READER_ORIENTATION_LANDSCAPE;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            orientation = PreferenceManager.READER_ORIENTATION_PORTRAIT;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    protected void switchMode() {
        Intent intent = getIntent();
        if (mode == PreferenceManager.READER_MODE_PAGE) {
            intent.setClass(this, StreamReaderActivity.class);
            intent.putExtra(Extra.EXTRA_MODE, PreferenceManager.READER_MODE_STREAM);
        } else {
            intent.setClass(this, PageReaderActivity.class);
            intent.putExtra(Extra.EXTRA_MODE, PreferenceManager.READER_MODE_PAGE);
        }
        finish();
        startActivity(intent);
    }

    protected void switchControl() {
        if (mProgressLayout.isShown()) {
            hideControl();
        } else {
            showControl();
        }
    }

    public static Intent createIntent(Context context, long id, List<Chapter> list, int mode) {
        Intent intent = getIntent(context, mode);
        intent.putExtra(Extra.EXTRA_ID, id);
        intent.putExtra(Extra.EXTRA_CHAPTER, new ArrayList<>(list));
        intent.putExtra(Extra.EXTRA_MODE, mode);
        return intent;
    }

    private static Intent getIntent(Context context, int mode) {
        if (mode == PreferenceManager.READER_MODE_PAGE) {
            return new Intent(context, PageReaderActivity.class);
        } else {
            return new Intent(context, StreamReaderActivity.class);
        }
    }

}
