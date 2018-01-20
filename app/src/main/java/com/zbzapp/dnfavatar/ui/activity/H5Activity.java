package com.zbzapp.dnfavatar.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import butterknife.BindView;
import com.qq.e.ads.interstitial.AbstractInterstitialADListener;
import com.qq.e.ads.interstitial.InterstitialAD;
import com.qq.e.comm.util.AdError;
import com.tencent.smtt.sdk.WebView;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.constant.Constants;
import com.zbzapp.dnfavatar.global.Extra;

/**
 * Created by kiefer on 2017/9/14.
 */

public class H5Activity  extends BackActivity {
        @BindView(R.id.webview)
        WebView mWebView;
        private String mUrl;
        InterstitialAD iad;

        public static void start(Context mContext,String url) {
                Intent intent = new Intent(mContext, H5Activity.class);
                intent.putExtra(Extra.EXTRA_IS_URL,url);
                mContext.startActivity(intent);
        }

        @Override
        protected void initData() {
        }


        @Override
        protected void initView() {
                mUrl = getIntent().getExtras().getString(Extra.EXTRA_IS_URL);
                if(!TextUtils.isEmpty(mUrl)){
                        mWebView.loadUrl(mUrl);
                }
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

        @Override
        protected int getLayoutRes() {
                return R.layout.activity_h5;
        }

        @Override
        protected String getDefaultTitle() {
                return "详情";
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
