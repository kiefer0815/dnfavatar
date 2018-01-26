package com.zbzapp.dnfavatar.presenter;

import android.support.v4.util.LongSparseArray;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.zbzapp.dnfavatar.App;
import com.zbzapp.dnfavatar.core.Download;
import com.zbzapp.dnfavatar.core.Storage;
import com.zbzapp.dnfavatar.manager.ComicManager;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.manager.TaskManager;
import com.zbzapp.dnfavatar.misc.Pair;
import com.zbzapp.dnfavatar.model.Chapter;
import com.zbzapp.dnfavatar.model.Comic;
import com.zbzapp.dnfavatar.model.MiniComic;
import com.zbzapp.dnfavatar.model.Task;
import com.zbzapp.dnfavatar.rx.RxBus;
import com.zbzapp.dnfavatar.rx.RxEvent;
import com.zbzapp.dnfavatar.saf.DocumentFile;
import com.zbzapp.dnfavatar.ui.view.SettingsView;
import com.zbzapp.dnfavatar.utils.ComicUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsPresenter extends BasePresenter<SettingsView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;
    private SourceManager mSourceManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mTaskManager = TaskManager.getInstance(mBaseView);
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    public void clearCache() {
        Fresco.getImagePipeline().clearDiskCaches();
    }

    public void moveFiles(DocumentFile dst) {
        mCompositeSubscription.add(Storage.moveRootDir(App.application.getContentResolver(),
                mBaseView.getAppInstance().getDocumentFile(), dst)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String msg) {
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, msg));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onExecuteFail();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mBaseView.onFileMoveSuccess();
                    }
                }));
    }

    private void updateKey(long key, List<Task> list) {
        for (Task task : list) {
            task.setKey(key);
        }
    }

    public void scanTask() {
        // Todo 重写一下
        mCompositeSubscription.add(Download.scan(App.application.getContentResolver(), mBaseView.getAppInstance().getDocumentFile())
                .doOnNext(new Action1<Pair<Comic, List<Task>>>() {
                    @Override
                    public void call(Pair<Comic, List<Task>> pair) {
                        Comic comic = mComicManager.load(pair.first.getSource(), pair.first.getCid());
                        if (comic == null) {
                            mComicManager.insert(pair.first);
                            updateKey(pair.first.getId(), pair.second);
                            mTaskManager.insertInTx(pair.second);
                            comic = pair.first;
                        } else {
                            comic.setDownload(System.currentTimeMillis());
                            mComicManager.update(comic);
                            updateKey(comic.getId(), pair.second);
                            mTaskManager.insertIfNotExist(pair.second);
                        }
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_INSERT, new MiniComic(comic)));
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, comic.getTitle()));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair<Comic, List<Task>>>() {
                    @Override
                    public void call(Pair<Comic, List<Task>> pair) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onExecuteFail();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mBaseView.onExecuteSuccess();
                    }
                }));
    }

    public void deleteTask() {
        // Todo 重写一下
        mBaseView.getAppInstance().getDocumentFile().refresh();
        mCompositeSubscription.add(Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                List<Comic> list = mComicManager.listDownload();
                list.addAll(mComicManager.listLocal());
                Pair<List<Comic>, List<Task>> pair = findInvalid(ComicUtils.buildComicMap(list));
                deleteInvalid(pair.first, pair.second);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String msg) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onExecuteFail();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mBaseView.onExecuteSuccess();
                    }
                }));
    }

    private Pair<List<Comic>, List<Task>> findInvalid(LongSparseArray<Comic> array) {
        List<Task> tList = new LinkedList<>();  // 无效任务列表
        List<Comic> cList = new LinkedList<>(); // 无效漫画列表
        Set<Long> set = new HashSet<>();        // 有效漫画 ID
        for (Task task : mTaskManager.listValid()) {
            Comic comic = array.get(task.getKey());
            if (comic == null) {
                tList.add(task);
            } else if (comic.getLocal()) {
                set.add(task.getKey());
            } else if (Download.getChapterDir(mBaseView.getAppInstance().getDocumentFile(),
                    comic, new Chapter(task.getTitle(), task.getPath()),
                    mSourceManager.getParser(comic.getSource()).getTitle()) == null) {
                tList.add(task);
            } else {
                set.add(task.getKey());
            }
        }
        for (int i = 0; i != array.size(); ++i) {
            if (!set.contains(array.keyAt(i))) {
                cList.add(array.valueAt(i));
            }
        }
        return Pair.create(cList, tList);
    }

    private void deleteInvalid(final List<Comic> cList, final List<Task> tList) {
        List<Long> list = new LinkedList<>();
        for (Comic comic : cList) {
            list.add(comic.getId());
        }
        mComicManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (Comic comic : cList) {
                    comic.setDownload(null);
                    mComicManager.updateOrDelete(comic);
                }
                mTaskManager.deleteInTx(tList);
            }
        });
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_CLEAR, list));
    }

}
