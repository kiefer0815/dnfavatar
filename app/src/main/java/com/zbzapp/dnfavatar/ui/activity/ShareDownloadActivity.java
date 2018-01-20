package com.zbzapp.dnfavatar.ui.activity;

import android.content.Context;
import android.content.Intent;
import com.zbzapp.dnfavatar.R;

/**
 * Created by kiefer on 2017/10/26.
 */

public class ShareDownloadActivity extends BackActivity {
        public static void start(Context mContext) {
                Intent intent = new Intent(mContext, ShareDownloadActivity.class);
                mContext.startActivity(intent);
        }

        @Override
        protected int getLayoutRes() {
                return R.layout.activity_share_download;
        }
        @Override
        protected String getDefaultTitle() {
                return "下载分享";
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
