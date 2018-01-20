package com.zbzapp.dnfavatar.ui.fragment.recyclerview.grid;

import android.os.Bundle;
import android.util.Log;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.constant.Constants;
import com.zbzapp.dnfavatar.model.MiniComic;
import com.zbzapp.dnfavatar.presenter.BasePresenter;
import com.zbzapp.dnfavatar.presenter.HistoryPresenter;
import com.zbzapp.dnfavatar.ui.fragment.dialog.MessageDialogFragment;
import com.zbzapp.dnfavatar.ui.view.HistoryView;
import com.zbzapp.dnfavatar.utils.HintUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class HistoryFragment extends GridFragment implements HistoryView ,NativeExpressAD.NativeExpressADListener{

    private static final int DIALOG_REQUEST_CLEAR = 1;
    private static final int DIALOG_REQUEST_INFO = 2;
    private static final int DIALOG_REQUEST_DELETE = 3;

    private static final int OPERATION_INFO = 0;
    private static final int OPERATION_DELETE = 1;

    private HistoryPresenter mPresenter;

    private static String TAG = HistoryFragment.class.getSimpleName();

    //*******************广告**********************

    private NativeExpressAD mADManager;
    private List<NativeExpressADView> mAdViewList;
    public static final int AD_COUNT = 1;    // 加载广告的条数，取值范围为[1, 10]
    public static int FIRST_AD_POSITION = 1; // 第一条广告的位置
    public static int ITEMS_PER_AD = 10;     // 每间隔10个条目插入一条广告
    private int mAdStartPosition = 0;
    
    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new HistoryPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    protected void performActionButtonClick() {
        if (mGridAdapter.getDateSet().isEmpty()) {
            return;
        }
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.history_clear_confirm, true, DIALOG_REQUEST_CLEAR);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_OPERATION:
                int index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                switch (index) {
                    case OPERATION_INFO:
                        showComicInfo(mPresenter.load(mSavedId), DIALOG_REQUEST_INFO);
                        break;
                    case OPERATION_DELETE:
                        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                                R.string.history_delete_confirm, true, DIALOG_REQUEST_DELETE);
                        fragment.setTargetFragment(this, 0);
                        fragment.show(getFragmentManager(), null);
                }
                break;
            case DIALOG_REQUEST_CLEAR:
                showProgressDialog();
                mPresenter.clear();
                break;
            case DIALOG_REQUEST_DELETE:
                showProgressDialog();
                mPresenter.delete(mSavedId);
                break;
        }
    }

    @Override
    public void onHistoryClearSuccess() {
        hideProgressDialog();
        mGridAdapter.clear();
        HintUtils.showToast(getActivity(), R.string.common_execute_success);
    }

    @Override
    public void onHistoryDelete(long id) {
        hideProgressDialog();
        mGridAdapter.removeItemById(mSavedId);
        HintUtils.showToast(getActivity(), R.string.common_execute_success);
    }

    @Override
    public void OnComicRestore(List<MiniComic> list) {
        List<Object> tmp =  new ArrayList<Object>();
        for (MiniComic comic :list){
            tmp.add(comic);
        }
        mGridAdapter.addAll(0,tmp);
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        super.onComicLoadSuccess(list);
        if(list.size() > 0){
            initNativeExpressAD();
        }
    }

    @Override
    public void onItemUpdate(MiniComic comic) {
        mGridAdapter.remove(comic);
        mGridAdapter.add(0, comic);
    }

    @Override
    protected int getActionButtonRes() {
        return R.drawable.ic_delete_white_24dp;
    }


    @Override
    protected String[] getOperationItems() {
        return new String[]{ getString(R.string.comic_info), getString(R.string.history_delete) };
    }
    //**********************广告相关****************************************8


    private void initNativeExpressAD() {
        final float density = getResources().getDisplayMetrics().density;
        ADSize adSize = new ADSize((int) (getResources().getDisplayMetrics().widthPixels / density), 130); // 宽、高的单位是dp。ADSize不支持MATCH_PARENT or WRAP_CONTENT，必须传入实际的宽高
        mADManager = new NativeExpressAD(getActivity(), adSize, Constants.APPID, Constants.NativeExpressPosID2, this);
        mADManager.loadAD(AD_COUNT);
    }

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
        mAdViewList = adList;
        for (int i = 0; i < mAdViewList.size(); i++) {
            int position = mAdStartPosition + FIRST_AD_POSITION + ITEMS_PER_AD * i;
            if (position < mGridAdapter.getItemCount()) {
                mAdViewPositionMap.put(mAdViewList.get(i), position); // 把每个广告在列表中位置记录下来
                mGridAdapter.addADViewToPosition(position, mAdViewList.get(i));
            }
        }
        mGridAdapter.notifyDataSetChanged();
        mAdStartPosition = mGridAdapter.getItemCount();
    }

    @Override
    public void onRenderFail(NativeExpressADView adView) {
        Log.i(TAG, "onRenderFail: " + adView.toString());
    }

    @Override
    public void onRenderSuccess(NativeExpressADView adView) {
        Log.i(TAG, "onRenderSuccess: " + adView.toString());
    }

    @Override
    public void onADExposure(NativeExpressADView adView) {
        Log.i(TAG, "onADExposure: " + adView.toString());
    }

    @Override
    public void onADClicked(NativeExpressADView adView) {
        Log.i(TAG, "onADClicked: " + adView.toString());
    }

    @Override
    public void onADClosed(NativeExpressADView adView) {
        Log.i(TAG, "onADClosed: " + adView.toString());
        if (mGridAdapter != null) {
            int removedPosition = mAdViewPositionMap.get(adView);
            mGridAdapter.removeADView(removedPosition, adView);
        }
    }

    @Override
    public void onADLeftApplication(NativeExpressADView adView) {
        Log.i(TAG, "onADLeftApplication: " + adView.toString());
    }

    @Override
    public void onADOpenOverlay(NativeExpressADView adView) {
        Log.i(TAG, "onADOpenOverlay: " + adView.toString());
    }

}

