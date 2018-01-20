package com.zbzapp.dnfavatar.ui.fragment.recyclerview.list;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.manager.PreferenceManager;
import com.zbzapp.dnfavatar.model.MiniComic;
import com.zbzapp.dnfavatar.presenter.BasePresenter;
import com.zbzapp.dnfavatar.presenter.FavoritePresenter;
import com.zbzapp.dnfavatar.ui.fragment.dialog.MessageDialogFragment;
import com.zbzapp.dnfavatar.ui.fragment.recyclerview.grid.GridFragment;
import com.zbzapp.dnfavatar.ui.view.FavoriteView;
import com.zbzapp.dnfavatar.utils.HintUtils;
import com.zbzapp.dnfavatar.utils.NotificationUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class FavoriteFragment extends ListFragment implements FavoriteView {

    private static final int DIALOG_REQUEST_UPDATE = 1;
    private static final int DIALOG_REQUEST_INFO = 2;
    private static final int DIALOG_REQUEST_DELETE = 3;

    private static final int OPERATION_INFO = 0;
    private static final int OPERATION_DELETE = 1;

    private FavoritePresenter mPresenter;
    private Notification.Builder mBuilder;
    private NotificationManager mManager;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new FavoritePresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
        mManager = NotificationUtils.getManager(getActivity());
        mListAdapter.setSymbol(true);
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBuilder != null) {
            NotificationUtils.cancelNotification(0, mManager);
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_OPERATION:
                int index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                switch (index) {

                    case OPERATION_DELETE:
                        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                                R.string.favorite_delete_confirm, true, DIALOG_REQUEST_DELETE);
                        fragment.setTargetFragment(this, 0);
                        fragment.show(getFragmentManager(), null);
                        break;
                }
                break;
            case DIALOG_REQUEST_UPDATE:
                checkUpdate();
                break;
            case DIALOG_REQUEST_DELETE:
                mPresenter.unfavoriteComic(mSavedId);
                HintUtils.showToast(getActivity(), R.string.common_execute_success);
                break;
        }
    }

    public void cancelAllHighlight() {
        mPresenter.cancelAllHighlight();
        mListAdapter.cancelAllHighlight();
    }

    private void checkUpdate() {
        if (mBuilder == null) {
            mPresenter.checkUpdate();
            mBuilder = NotificationUtils.getBuilder(getActivity(), R.drawable.ic_sync_white_24dp,
                    R.string.favorite_check_update_doing, true, 0, 0, true);
            NotificationUtils.notifyBuilder(0, mManager, mBuilder);
        } else {
            HintUtils.showToast(getActivity(), R.string.favorite_check_update_doing);
        }
    }

    @Override
    protected void performActionButtonClick() {
        if (mListAdapter.getDateSet().isEmpty()) {
            return;
        }
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.favorite_check_update_confirm, true, DIALOG_REQUEST_UPDATE);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        super.onComicLoadSuccess(list);
        WifiManager manager =
                (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled() &&
                mPreference.getBoolean(PreferenceManager.PREF_OTHER_CHECK_UPDATE, false)) {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTimeInMillis(mPreference.getLong(PreferenceManager.PREF_OTHER_CHECK_UPDATE_LAST, 0));
            if (day != calendar.get(Calendar.DAY_OF_YEAR)) {
                mPreference.putLong(PreferenceManager.PREF_OTHER_CHECK_UPDATE_LAST, System.currentTimeMillis());
                checkUpdate();
            }
        }
    }

    @Override
    public void OnComicFavorite(MiniComic comic) {
        mListAdapter.add(mListAdapter.findFirstNotHighlight(), comic);
    }

    @Override
    public void OnComicRestore(List<MiniComic> list) {
        mListAdapter.addAll(mListAdapter.findFirstNotHighlight(), list);
    }

    @Override
    public void OnComicUnFavorite(long id) {
        mListAdapter.removeItemById(id);
    }

    @Override
    public void onComicCheckSuccess(MiniComic comic, int progress, int max) {
        if (comic != null) {
            mListAdapter.remove(comic);
            mListAdapter.add(0, comic);
        }
        mBuilder.setProgress(max, progress, false);
        NotificationUtils.notifyBuilder(0, mManager, mBuilder);
    }

    @Override
    public void onComicCheckFail() {
        NotificationUtils.setBuilder(getActivity(), mBuilder, R.string.favorite_check_update_fail, false);
        NotificationUtils.notifyBuilder(0, mManager, mBuilder);
        mBuilder = null;
    }

    @Override
    public void onComicCheckComplete() {
        NotificationUtils.setBuilder(getActivity(), mBuilder, R.string.favorite_check_update_done, false);
        NotificationUtils.notifyBuilder(0, mManager, mBuilder);
        NotificationUtils.cancelNotification(0, mManager);
        mBuilder = null;
    }

    @Override
    public void onHighlightCancel(MiniComic comic) {
        mListAdapter.moveItemTop(comic);
    }

    @Override
    public void onComicRead(MiniComic comic) {
        mListAdapter.moveItemTop(comic);
    }

    @Override
    protected int getActionButtonRes() {
        return R.drawable.ic_sync_white_24dp;
    }

    @Override
    protected String[] getOperationItems() {
        return new String[]{ getString(R.string.comic_info), getString(R.string.favorite_delete) };
    }

}
