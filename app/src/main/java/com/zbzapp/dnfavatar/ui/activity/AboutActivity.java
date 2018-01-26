package com.zbzapp.dnfavatar.ui.activity;

import android.content.pm.PackageInfo;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.manager.UpdateManager;
import com.zbzapp.dnfavatar.presenter.AboutPresenter;
import com.zbzapp.dnfavatar.presenter.BasePresenter;
import com.zbzapp.dnfavatar.ui.view.AboutView;
import com.zbzapp.dnfavatar.utils.StringUtils;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class AboutActivity extends BackActivity implements AboutView {

    @BindView(R.id.about_update_summary) TextView mUpdateText;
    @BindView(R.id.about_version_name) TextView mVersionName;
    @BindView(R.id.about_layout) View mLayoutView;

    private AboutPresenter mPresenter;
    private boolean update = false;
    private boolean checking = false;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new AboutPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            mVersionName.setText(StringUtils.format("version: %s", info.versionName));
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    @OnClick(R.id.about_update_btn) void onUpdateClick() {
        UpdateManager.getInstance().init(this);
        UpdateManager.getInstance().checkUpdate(true);
        TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/patch_signed_7zip.apk");

    }

    @Override
    public void onUpdateNone() {
        mUpdateText.setText(R.string.about_update_latest);
        checking = false;
    }

    @Override
    public void onUpdateReady() {
        mUpdateText.setText(R.string.about_update_download);
        checking = false;
        update = true;
    }

    @Override
    public void onCheckError() {
        mUpdateText.setText(R.string.about_update_fail);
        checking = false;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.drawer_about);
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_about;
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
