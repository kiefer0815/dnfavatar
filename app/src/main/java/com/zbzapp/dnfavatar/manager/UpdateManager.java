package com.zbzapp.dnfavatar.manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.zbzapp.dnfavatar.App;
import com.zbzapp.dnfavatar.BuildConfig;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.model.AppUpdateBean;
import com.zbzapp.dnfavatar.utils.CommonUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.io.File;

/**
 * Created by kiefer on 2016/7/7.
 */
public class UpdateManager {

        private Context mContext;
        private static UpdateManager sSelf = null;
        private int preProgress = 0;
        private int NOTIFY_ID = 1000;
        private NotificationCompat.Builder builder;
        private NotificationManager notificationManager;
        private static boolean isDownLoading = false;
        private static boolean isBackGround = false;
        private static String UPDATE_URL ="";

        public synchronized static UpdateManager getInstance() {
                if (sSelf == null) {
                        sSelf = new UpdateManager();
                }
                return sSelf;
        }

        public void init(Context context) {
                mContext = context;
        }

        public static Observable<String> check() {
                return Observable.create(new Observable.OnSubscribe<String>() {
                        @Override
                        public void call(Subscriber<? super String> subscriber) {
                                OkHttpClient client = App.getHttpClient();
                                Request request = new Request.Builder().url(UPDATE_URL).build();
                                Response response = null;
                                try {
                                        response = client.newCall(request).execute();
                                        if (response.isSuccessful()) {
                                                String json = response.body().string();
                                                subscriber.onNext(json);
                                                subscriber.onCompleted();
                                        }
                                } catch (Exception e) {
                                        e.printStackTrace();
                                } finally {
                                        if (response != null) {
                                                response.close();
                                        }
                                }
                                subscriber.onError(new Exception());
                        }
                }).subscribeOn(Schedulers.io());
        }

        public void checkUpdate(final boolean isToast){
                       check()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<String>() {
                                @Override
                                public void call(String jsonStr) {
                                        try {
                                                AppUpdateBean mAppUpdateModel = (AppUpdateBean) JSON.parseObject(jsonStr, AppUpdateBean.class);
                                                handleAppUpdate(mAppUpdateModel,isToast);
                                        } catch (Exception e) {
                                                e.printStackTrace();
                                        }finally {
                                        }
                                }
                        }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                }
                        });
        }

        public void handleAppUpdate(AppUpdateBean mAppUpdateModel, boolean isToast) {
                if (mAppUpdateModel == null)
                        return;
                /**
                 * 在这里请求后台接口，获取更新的内容和最新的版本号
                 */
                // 版本的更新信息
                String version_info = mAppUpdateModel.getUpdateDesc();
                int mVersion_code = CommonUtils.getVersionCode(mContext);// 当前的版本号
                int nVersion_code = mAppUpdateModel.getAppVersion();
                String updateChannel = mAppUpdateModel.getChannel();
                PreferenceManager manager = new PreferenceManager(App.applicationContext);





                //指定渠道升级
                if (updateChannel != null && !updateChannel.equals("all")) {
                        if (CommonUtils.getApplicationChannel(mContext) == null || !updateChannel
                                .equals(CommonUtils.getApplicationChannel(mContext))) {
                                if (isToast) {
                                        Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_SHORT).show();
                                }
                                return;
                        }
                }

                //版本号判断
                if (mVersion_code < nVersion_code) {
                        // 是否后台更新
                        if (mAppUpdateModel.isUpdateBackground()) {
                                isBackGround = true;
                                downLoadFile(mAppUpdateModel.getApkUrl());
                        } else if (mAppUpdateModel.isForce()) {
                                showNoticeDialog(version_info, true, mAppUpdateModel.getApkUrl());
                        } else {
                                showNoticeDialog(version_info, false, mAppUpdateModel.getApkUrl());
                        }

                } else {
                        if (isToast) {
                                Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_SHORT).show();
                        }
                }
        }

        /**
         * 显示更新对话框
         *
         * @param version_info
         */
        private void showNoticeDialog(String version_info, boolean force, final String url) {
                // 构造对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
                builder.setTitle("更新提示");
                builder.setMessage(version_info);
                // 更新
                builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                downLoadFile(url);
                        }
                });
                if (!force) {
                        // 稍后更新
                        builder.setNegativeButton("以后更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                }
                        });
                }
                Dialog noticeDialog = builder.create();

                noticeDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
                noticeDialog.show();
        }

        private void downLoadFile(String url) {
                initNotification();
                if (TextUtils.isEmpty(url))
                        return;
                FileDownloader.getImpl().create(url)
                        .setPath(FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "download"
                                + File.separator + "cartoon.apk")
                        .setListener(new FileDownloadListener() {
                                @Override
                                protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                }

                                @Override
                                protected void connected(BaseDownloadTask task, String etag, boolean isContinue,
                                        int soFarBytes, int totalBytes) {
                                }

                                @Override
                                protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                        updateNotification(soFarBytes * 100 / totalBytes);
                                }

                                @Override
                                protected void blockComplete(BaseDownloadTask task) {
                                }

                                @Override
                                protected void retry(final BaseDownloadTask task, final Throwable ex,
                                        final int retryingTimes, final int soFarBytes) {
                                }

                                @Override
                                protected void completed(BaseDownloadTask task) {
                                        cancelNotification();
                                        installApk(new File(task.getTargetFilePath()));
                                        isDownLoading = false;
                                }

                                @Override
                                protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                                }

                                @Override
                                protected void error(BaseDownloadTask task, Throwable e) {
                                        cancelNotification();
                                        isDownLoading = false;
                                }

                                @Override
                                protected void warn(BaseDownloadTask task) {
                                }
                        }).start();
        }

        /**
         * 安装软件
         *
         * @param apkFile
         */
        private void installApk(File apkFile) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                //判断是否是AndroidN以及更高的版本
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri contentUri = FileProvider
                                .getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
                        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                // 执行意图进行安装
                mContext.startActivity(intent);
        }

        /**
         * 初始化Notification通知
         */
        public void initNotification() {
                if (isBackGround)
                        return;
                builder = new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText("0%")
                        .setContentTitle("正在下载")
                        .setProgress(100, 0, false);
                notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFY_ID, builder.build());
        }

        /**
         * 更新通知
         */
        public void updateNotification(long progress) {
                if (isBackGround)
                        return;
                int currProgress = (int) progress;
                if (preProgress < currProgress) {
                        builder.setContentText(progress + "%");
                        builder.setProgress(100, (int) progress, false);
                        notificationManager.notify(NOTIFY_ID, builder.build());
                }
                preProgress = (int) progress;
        }

        /**
         * 取消通知
         */
        public void cancelNotification() {
                if (isBackGround)
                        return;
                notificationManager.cancel(NOTIFY_ID);
        }
}
