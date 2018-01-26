package com.zbzapp.dnfavatar.ui.fragment;

import android.app.Fragment;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.zbzapp.dnfavatar.App;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.manager.PreferenceManager;
import com.zbzapp.dnfavatar.presenter.BasePresenter;
import com.zbzapp.dnfavatar.ui.activity.BaseActivity;
import com.zbzapp.dnfavatar.ui.view.BaseView;
import com.zbzapp.dnfavatar.utils.ThemeUtils;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseFragment extends Fragment implements BaseView {

    @Nullable @BindView(R.id.custom_progress_bar) ProgressBar mProgressBar;
    protected PreferenceManager mPreference;
    private Unbinder unbinder;
    private BasePresenter mBasePresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        unbinder = ButterKnife.bind(this, view);
        mPreference = App.getInstance().getPreferenceManager();
        mBasePresenter = initPresenter();
        initProgressBar();
        initView();
        initData();
        return view;
    }

    @Override
    public void onDestroyView() {
        if (mBasePresenter != null) {
            mBasePresenter.detachView();
        }
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public App getAppInstance() {
        return App.getInstance();
    }

    @Override
    public void onNightSwitch() {}

    private void initProgressBar() {
        if (mProgressBar != null) {
            int resId = ThemeUtils.getResourceId(getActivity(), R.attr.colorAccent);
            mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), resId), PorterDuff.Mode.SRC_ATOP);
        }
    }

    protected void initView() {}

    protected void initData() {}

    protected BasePresenter initPresenter() {
        return null;
    }

    protected abstract @LayoutRes int getLayoutRes();

    protected void showProgressDialog() {
        ((BaseActivity) getActivity()).showProgressDialog();
    }

    protected void hideProgressDialog() {
        ((BaseActivity) getActivity()).hideProgressDialog();
    }

    protected void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
