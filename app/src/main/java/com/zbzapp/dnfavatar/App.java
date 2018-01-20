package com.zbzapp.dnfavatar;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.liulishuo.filedownloader.FileDownloader;
import com.qq.e.ads.cfg.MultiProcessFlag;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.*;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.tencent.smtt.sdk.QbSdk;
import com.zbzapp.dnfavatar.component.AppGetter;
import com.zbzapp.dnfavatar.constant.Constants;
import com.zbzapp.dnfavatar.core.Storage;
import com.zbzapp.dnfavatar.fresco.ControllerBuilderProvider;
import com.zbzapp.dnfavatar.helper.DBOpenHelper;
import com.zbzapp.dnfavatar.helper.UpdateHelper;
import com.zbzapp.dnfavatar.manager.PreferenceManager;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.manager.UserManager;
import com.zbzapp.dnfavatar.model.DaoMaster;
import com.zbzapp.dnfavatar.model.DaoSession;
import com.zbzapp.dnfavatar.saf.DocumentFile;
import com.zbzapp.dnfavatar.ui.activity.LockScreenActivity;
import com.zbzapp.dnfavatar.ui.adapter.GridAdapter;
import okhttp3.OkHttpClient;
import org.greenrobot.greendao.identityscope.IdentityScopeType;

import java.io.File;

/**
 * Created by Hiroshi on 2016/7/5.
 */
public class App extends MultiDexApplication implements AppGetter {
    public static volatile Context applicationContext = null;
    public static int mWidthPixels;
    public static int mHeightPixels;
    public static int mCoverWidthPixels;
    public static int mCoverHeightPixels;
    public static int mLargePixels;

    private static OkHttpClient mHttpClient;

    private DocumentFile mDocumentFile;
    private PreferenceManager mPreferenceManager;
    private ControllerBuilderProvider mBuilderProvider;
    private RecyclerView.RecycledViewPool mRecycledPool;
    private DBOpenHelper mOpenHelper;
    private DaoSession mDaoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();

        mOpenHelper = new DBOpenHelper(this, "cimoc.db");
        UpdateHelper.update(getPreferenceManager(), getDaoSession());
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                .newBuilder(applicationContext, getHttpClient())
                .build();
        Fresco.initialize(this,config);
        initPixels();
        MultiProcessFlag.setMultiProcess(true);
        registerActivityLifecycleCallbacks(new AppForeBackStatusCallback());
        FileDownloader.init(applicationContext);
        createCacheDir();
        QbSdk.initX5Environment(this,null);
    }

    public void createCacheDir() {
        File file = new File(applicationContext.getFilesDir() + "/cache");
        if (!file.exists())
            file.mkdirs();

        File file_download = new File(applicationContext.getFilesDir() + "/download");
        if (!file_download.exists())
            file_download.mkdirs();
    }

    @Override
    public App getAppInstance() {
        return this;
    }

    //static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreater(new DefaultRefreshHeaderCreater() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.colorPrimaryBlue, android.R.color.white);//全局设置主题颜色
                return new ClassicsHeader(context).setSpinnerStyle(SpinnerStyle.Translate);//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreater(new DefaultRefreshFooterCreater() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.Translate);
            }
        });
    }

    private void initPixels() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        mWidthPixels = metrics.widthPixels;
        mHeightPixels = metrics.heightPixels;
        mCoverWidthPixels = mWidthPixels / 3;
        mCoverHeightPixels = mHeightPixels * mCoverWidthPixels / mWidthPixels;
        mLargePixels = 3 * metrics.widthPixels * metrics.heightPixels;
    }

    public void initRootDocumentFile() {
        String uri = mPreferenceManager.getString(PreferenceManager.PREF_OTHER_STORAGE);
        mDocumentFile = Storage.initRoot(this, uri);
    }

    public DocumentFile getDocumentFile() {
        if (mDocumentFile == null) {
            initRootDocumentFile();
        }
        return mDocumentFile;
    }

    public DaoSession getDaoSession() {
        if (mDaoSession == null) {
            mDaoSession = new DaoMaster(mOpenHelper.getWritableDatabase()).newSession(IdentityScopeType.None);
        }
        return mDaoSession;
    }

    public PreferenceManager getPreferenceManager() {
        if (mPreferenceManager == null) {
            mPreferenceManager = new PreferenceManager(getApplicationContext());
        }
        return mPreferenceManager;
    }

    public RecyclerView.RecycledViewPool getGridRecycledPool() {
        if (mRecycledPool == null) {
            mRecycledPool = new RecyclerView.RecycledViewPool();
            mRecycledPool.setMaxRecycledViews(GridAdapter.TYPE_GRID, 20);
        }
        return mRecycledPool;
    }

    public ControllerBuilderProvider getBuilderProvider() {
        if (mBuilderProvider == null) {
            mBuilderProvider = new ControllerBuilderProvider(getApplicationContext(),
                    SourceManager.getInstance(this).new HeaderGetter(), true);
        }
        return mBuilderProvider;
    }

    public static OkHttpClient getHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new OkHttpClient();
        }
        return mHttpClient;
    }

    public class AppForeBackStatusCallback implements Application.ActivityLifecycleCallbacks {

        /**
         * 活动的Activity数量,为1时，APP处于前台状态，为0时，APP处于后台状态
         */
        private int activityCount = 0;

        /**
         * 最后一次可见的Activity
         * 用于比对Activity，这样可以排除启动应用时的这种特殊情况，
         * 如果启动应用时也需要锁屏等操作，请在启动页里进行操作。
         */
        private Activity lastVisibleActivity;

        /**
         * 最大无需解锁时长 5分钟 单位：毫秒
         */
        private final static long MAX_UNLOCK_DURATION = 0;

        /**
         * 最后一次离开应用时间 单位：毫秒
         */
        private long lastTime;

        @Override

        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            activityCount++;
        }

        @Override
        public void onActivityResumed(Activity activity) {
            // 后台进程切换到前台进程,不包含启动应用时的这种特殊情况
            //最后一次可见Activity是被唤醒的Activity && 活动的Activity数量为1
            if (lastVisibleActivity == activity && activityCount == 1) {
                //Background -> Foreground , do something
                startLockScreen(activity);
            }

            lastVisibleActivity = activity;
        }

        /**
         * 打开手势密码
         *
         * @param activity Activity
         */
        private void startLockScreen(Activity activity) {
            if (lockScreen(activity)) {
                LockScreenActivity.start(activity,"1");
            }
        }

        /**
         * 锁屏
         *
         * @param activity Activity
         * @return true 锁屏，反之不锁屏
         */
        private boolean lockScreen(Activity activity) {
            //解锁未超时，不锁屏
            if (!unlockTimeout())
                return false;
            // 当前Activity是锁屏Activity或登录Activity，不锁屏
            if (activity instanceof LockScreenActivity)
                return false;
            //不满足其它条件，不锁屏，#备用#
            if (!otherCondition()) {
                return false;
            }
            //锁屏
            return true;
        }

        /**
         * 由后台切到前台时，解锁时间超时
         *
         * @return 时间间隔大于解锁时长为true，反之为false
         */
        private boolean unlockTimeout() {
            //当前时间和上次离开应用时间间隔
            long dTime = System.currentTimeMillis() - lastTime;
            return dTime > MAX_UNLOCK_DURATION;
        }

        /**
         * 其它条件
         *
         * @return boolean
         */
        private boolean otherCondition() {
            if(TextUtils.isEmpty(UserManager.getExtraInfo(Constants.LOCK_SCREEN_STRING,""))){
                return false;
            }
            return true;
        }


        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityCount--;
            lastTime = System.currentTimeMillis();
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }


}
