package com.zbzapp.dnfavatar.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import com.zbzapp.dnfavatar.App;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.component.AppGetter;
import com.zbzapp.dnfavatar.core.Download;
import com.zbzapp.dnfavatar.core.Manga;
import com.zbzapp.dnfavatar.global.Extra;
import com.zbzapp.dnfavatar.manager.PreferenceManager;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.manager.TaskManager;
import com.zbzapp.dnfavatar.misc.Pair;
import com.zbzapp.dnfavatar.model.ImageUrl;
import com.zbzapp.dnfavatar.model.Task;
import com.zbzapp.dnfavatar.parser.Parser;
import com.zbzapp.dnfavatar.rx.RxBus;
import com.zbzapp.dnfavatar.rx.RxEvent;
import com.zbzapp.dnfavatar.saf.DocumentFile;
import com.zbzapp.dnfavatar.utils.DocumentUtils;
import com.zbzapp.dnfavatar.utils.NotificationUtils;
import com.zbzapp.dnfavatar.utils.StringUtils;
import okhttp3.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadService extends Service implements AppGetter {

    private LongSparseArray<Pair<Worker, Future>> mWorkerArray;
    private ExecutorService mExecutorService;
    private OkHttpClient mHttpClient;
    private Notification.Builder builder;
    private NotificationManager notification;
    private TaskManager mTaskManager;
    private SourceManager mSourceManager;
    private ContentResolver mContentResolver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager manager = App.getInstance().getPreferenceManager();
        int num = manager.getInt(PreferenceManager.PREF_DOWNLOAD_THREAD, 1);
        mWorkerArray = new LongSparseArray<>();
        mExecutorService = Executors.newFixedThreadPool(num);
        mHttpClient = App.getHttpClient();
        mTaskManager = TaskManager.getInstance(this);
        mSourceManager = SourceManager.getInstance(this);
        mContentResolver = getContentResolver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_START));
            if (notification == null) {
                notification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                builder = NotificationUtils.getBuilder(this, R.drawable.ic_file_download_white_24dp,
                        R.string.download_service_doing, true);
                NotificationUtils.notifyBuilder(1, notification, builder);
            }
            List<Task> list =  intent.getParcelableArrayListExtra(Extra.EXTRA_TASK);
            for (Task task : list) {
                Worker worker = new Worker(task);
                Future future = mExecutorService.submit(worker);
                addWorker(task.getId(), worker, future);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notification != null) {
            mExecutorService.shutdownNow();
            notifyCompleted();
        }
    }

    @Override
    public App getAppInstance() {
        return App.getInstance();
    }

    public synchronized void addWorker(long id, Worker worker, Future future) {
        if (mWorkerArray.get(id) == null) {
            mWorkerArray.put(id, Pair.create(worker, future));
        }
    }

    public synchronized void removeDownload(long id) {
        Pair<Worker, Future> pair = mWorkerArray.get(id);
        if (pair != null) {
            pair.second.cancel(true);
            mWorkerArray.remove(id);
        }
    }

    public synchronized void completeDownload(long id) {
        mWorkerArray.remove(id);
        if (mWorkerArray.size() == 0) {
            notifyCompleted();
            stopSelf();
        }
    }

    private void notifyCompleted() {
        if (notification != null) {
            NotificationUtils.setBuilder(this, builder, R.string.download_service_complete, false);
            NotificationUtils.notifyBuilder(1, notification, builder);
            notification = null;
        }
        mWorkerArray.clear();
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_STOP));
    }

    public synchronized void initTask(List<Task> list) {
        for (Task task : list) {
            Pair<Worker, Future> pair = mWorkerArray.get(task.getId());
            if (pair != null) {
                task.setState(pair.first.mTask.getState());
            }
        }
    }

    public class Worker implements Runnable {

        private Task mTask;
        private Parser mParse;

        Worker(Task task) {
            mTask = task;
            mParse = mSourceManager.getParser(task.getSource());
        }

        @Override
        public void run() {
            try {
                List<ImageUrl> list = onDownloadParse();
                int size = list.size();
                if (size != 0) {
                    DocumentFile dir = Download.updateChapterIndex(mContentResolver, getAppInstance().getDocumentFile(), mTask);
                    if (dir != null) {
                        mTask.setMax(size);
                        mTask.setState(Task.STATE_DOING);
                        boolean success = false;
                        for (int i = mTask.getProgress(); i < size; ++i) {
                            onDownloadProgress(i);
                            ImageUrl image = list.get(i);
                            int count = 0;  // 单页下载错误次数
                            success = false; // 是否下载成功
                            while (count++ < 20 && !success) {
                                String[] urls = image.getUrls();
                                for (int j = 0; !success && j < urls.length; ++j) {
                                    String url = image.isLazy() ? Manga.getLazyUrl(mParse, urls[j]) : urls[j];
                                    Request request = buildRequest(mParse.getHeader(), url);
                                    success = RequestAndWrite(dir, request, i + 1, url);
                                }
                            }
                            if (!success) {     // 单页下载错误
                                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, mTask.getId()));
                                break;
                            }
                        }
                        if (success) {
                            onDownloadProgress(size);
                        }
                    } else {
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, mTask.getId()));
                    }
                } else {
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, mTask.getId()));
                }
            } catch (InterruptedIOException e) {
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_PAUSE, mTask.getId()));
            }

            completeDownload(mTask.getId());
        }

        private boolean RequestAndWrite(DocumentFile parent, Request request, int num, String url) throws InterruptedIOException {
            if (request != null) {
                Response response = null;
                try {
                    response = mHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String displayName = buildFileName(num, url);
                        DocumentFile file = DocumentUtils.getOrCreateFile(parent, displayName);
                        DocumentUtils.writeBinaryToFile(mContentResolver, file, response.body().byteStream());
                        return true;
                    }
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                } catch (InterruptedIOException e) {
                    // 由暂停下载引发，需要抛出以便退出外层循环，结束任务
                    throw e;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
            }
            return false;
        }

        private Request buildRequest(Headers headers, String url) {
            if (StringUtils.isEmpty(url)) {
                return null;
            }

            return new Request.Builder()
                    .cacheControl(new CacheControl.Builder().noStore().build())
                    .headers(headers)
                    .url(url)
                    .get()
                    .build();
        }

        private String buildFileName(int num, String url) {
            String suffix = StringUtils.split(url, "\\.", -1);
            if (suffix == null) {
                suffix = "jpg";
            } else {
                suffix = suffix.split("\\?")[0];
            }
            return StringUtils.format("%03d.%s", num, suffix);
        }

        private List<ImageUrl> onDownloadParse() throws InterruptedIOException {
            mTask.setState(Task.STATE_PARSE);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_PARSE, mTask.getId()));
            return Manga.getImageUrls(mParse, mTask.getCid(), mTask.getPath());
        }

        private void onDownloadProgress(int progress) {
            mTask.setProgress(progress);
            mTaskManager.update(mTask);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_PROCESS, mTask.getId(), progress, mTask.getMax()));
        }

    }

    public class DownloadServiceBinder extends Binder {

        public DownloadService getService() {
            return DownloadService.this;
        }

    }

    public static Intent createIntent(Context context, Task task) {
        ArrayList<Task> list = new ArrayList<>(1);
        list.add(task);
        return createIntent(context, list);
    }

    public static Intent createIntent(Context context, ArrayList<Task> list) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putParcelableArrayListExtra(Extra.EXTRA_TASK, list);
        return intent;
    }

}
