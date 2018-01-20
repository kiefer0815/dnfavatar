package com.zbzapp.dnfavatar.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import butterknife.BindView;
import butterknife.OnClick;
import com.qq.e.ads.interstitial.AbstractInterstitialADListener;
import com.qq.e.ads.interstitial.InterstitialAD;
import com.qq.e.comm.util.AdError;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.constant.Constants;
import com.zbzapp.dnfavatar.global.Extra;
import com.zbzapp.dnfavatar.manager.PreferenceManager;
import com.zbzapp.dnfavatar.misc.Pair;
import com.zbzapp.dnfavatar.model.Chapter;
import com.zbzapp.dnfavatar.ui.adapter.BaseAdapter;
import com.zbzapp.dnfavatar.ui.adapter.ChapterAdapter;
import com.zbzapp.dnfavatar.ui.widget.ViewUtils;
import com.zbzapp.dnfavatar.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Hiroshi on 2016/11/14.
 */

public class ChapterActivity extends BackActivity implements BaseAdapter.OnItemClickListener {

    @BindView(R.id.chapter_recycler_view) RecyclerView mRecyclerView;

    private ChapterAdapter mChapterAdapter;
    private boolean isAscendMode;
    private boolean isButtonMode;
    private Handler mHandler = new Handler();
    private RecyclerView.OnItemTouchListener mListener = new CustomTouchListener();
    private RecyclerView.ItemDecoration mDecoration;
    InterstitialAD iad;

    @Override
    protected void initView() {
        isButtonMode = mPreference.getBoolean(PreferenceManager.PREF_CHAPTER_BUTTON_MODE, false);
        mChapterAdapter = new ChapterAdapter(this, getAdapterList());
        mDecoration = mChapterAdapter.getItemDecoration();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(mChapterAdapter);
        switchMode();
        showAD();
    }

    private InterstitialAD getIAD() {
        if (iad == null) {
            iad = new InterstitialAD(this, Constants.APPID, Constants.InterteristalPosID);
        }
        return iad;
    }

    private void showAD() {
        getIAD().setADListener(new AbstractInterstitialADListener() {

            @Override
            public void onNoAD(AdError error) {
                Log.i(
                        "AD_DEMO",
                        String.format("LoadInterstitialAd Fail, error code: %d, error msg: %s",
                                error.getErrorCode(), error.getErrorMsg()));
            }

            @Override
            public void onADReceive() {
                Log.i("AD_DEMO", "onADReceive");
                iad.showAsPopupWindow();
            }
        });
        iad.loadAD();
    }

    private void switchMode() {
        mChapterAdapter.setButtonMode(isButtonMode);
        if (isButtonMode) {
            mChapterAdapter.setOnItemClickListener(null);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            mRecyclerView.addItemDecoration(mDecoration);
            mRecyclerView.addOnItemTouchListener(mListener);
            mRecyclerView.setPadding((int) ViewUtils.dpToPixel(4, this), (int) ViewUtils.dpToPixel(10, this), (int) ViewUtils.dpToPixel(4, this), 0);
        } else {
            mChapterAdapter.setOnItemClickListener(this);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.removeItemDecoration(mDecoration);
            mRecyclerView.removeOnItemTouchListener(mListener);
            mRecyclerView.setPadding(0, 0, 0, 0);
        }
    }

    private List<Pair<Chapter, Boolean>> getAdapterList() {
        isAscendMode = mPreference.getBoolean(PreferenceManager.PREF_CHAPTER_ASCEND_MODE, false);
        List<Chapter> list = getIntent().getParcelableArrayListExtra(Extra.EXTRA_CHAPTER);
        List<Pair<Chapter, Boolean>> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); ++i) {
            result.add(Pair.create(list.get(i), list.get(i).isDownload()));
        }
        if (isAscendMode) {
            Collections.reverse(result);
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chapter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
                case R.id.chapter_all:
                    for (Pair<Chapter, Boolean> pair : mChapterAdapter.getDateSet()) {
                        pair.second = true;
                    }
                    mChapterAdapter.notifyDataSetChanged();
                    break;
                case R.id.chapter_sort:
                    mChapterAdapter.reverse();
                    isAscendMode = !isAscendMode;
                    mPreference.putBoolean(PreferenceManager.PREF_CHAPTER_ASCEND_MODE, isAscendMode);
                    break;
                case R.id.chapter_switch_view:
                    isButtonMode = !isButtonMode;
                    switchMode();
                    mPreference.putBoolean(PreferenceManager.PREF_CHAPTER_BUTTON_MODE, isButtonMode);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Pair<Chapter, Boolean> pair = mChapterAdapter.getItem(position);
        if (!pair.first.isDownload()) {
            pair.second = !pair.second;
            mChapterAdapter.notifyItemChanged(position);
        }
    }

    @OnClick(R.id.chapter_action_button) void onActionButtonClick() {
        ArrayList<Chapter> list = new ArrayList<>();
        for (Pair<Chapter, Boolean> pair : mChapterAdapter.getDateSet()) {
            if (!pair.first.isDownload() && pair.second) {
                list.add(pair.first);
            }
        }

        if (list.isEmpty()) {
            showSnackbar(R.string.chapter_download_empty);
        } else if (PermissionUtils.hasStoragePermission(this)) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Extra.EXTRA_CHAPTER, list);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            showSnackbar(R.string.chapter_download_perm_fail);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_chapter;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.chapter);
    }

    public static Intent createIntent(Context context, ArrayList<Chapter> list) {
        Intent intent = new Intent(context, ChapterActivity.class);
        intent.putExtra(Extra.EXTRA_CHAPTER, list);
        return intent;
    }

    class CustomTouchListener implements RecyclerView.OnItemTouchListener {
        private int mLastPosition = -1;
        private boolean isLongPress = false;

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            if (isLongPress) {
                return true;
            }

            int pos = rv.getChildAdapterPosition(rv.findChildViewUnder(e.getX(), e.getY()));
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastPosition = pos;
                    if (mLastPosition != -1) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isLongPress = true;
                                update(mLastPosition);
                            }
                        }, 500);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mLastPosition != pos) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mHandler.removeCallbacksAndMessages(null);
                    if (pos != -1 && mLastPosition == pos) {
                        update(pos);
                    }
                    break;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            int pos = rv.getChildAdapterPosition(rv.findChildViewUnder(e.getX(), e.getY()));
            switch (e.getAction()){
                case MotionEvent.ACTION_MOVE:
                    if (pos != -1 && mLastPosition != pos) {
                        update(pos);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isLongPress = false;
                    break;
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }

        private void update(int pos) {
            mLastPosition = pos;
            mChapterAdapter.getItem(pos).second = !mChapterAdapter.getItem(pos).second;
            mChapterAdapter.notifyItemChanged(pos);
        }
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
