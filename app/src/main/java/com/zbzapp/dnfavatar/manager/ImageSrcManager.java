package com.zbzapp.dnfavatar.manager;

import com.zbzapp.dnfavatar.component.AppGetter;
import com.zbzapp.dnfavatar.model.ImageSrc;
import com.zbzapp.dnfavatar.model.ImageSrcDao;
import com.zbzapp.dnfavatar.model.ImageUrl;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by kiefer on 2017/9/15.
 */

public class ImageSrcManager {

        private static ImageSrcManager mInstance;

        private ImageSrcDao mImageSrcDao;

        private ImageSrcManager(AppGetter getter) {
                mImageSrcDao = getter.getAppInstance().getDaoSession().getImageSrcDao();
        }

        public void runInTx(Runnable runnable) {
                mImageSrcDao.getSession().runInTx(runnable);
        }

        public <T> T callInTx(Callable<T> callable) {
                return mImageSrcDao.getSession().callInTxNoException(callable);
        }

        public static ImageSrcManager getInstance(AppGetter getter) {
                if (mInstance == null) {
                        synchronized (ImageSrcManager.class) {
                                if (mInstance == null) {
                                        mInstance = new ImageSrcManager(getter);
                                }
                        }
                }
                return mInstance;
        }

        public List<ImageSrc> listImageSrc(Long comicid) {
                return mImageSrcDao.queryBuilder()
                        .where(ImageSrcDao.Properties.Comicid.eq(comicid))
                        .list();
        }

        public Observable<List<ImageUrl>> listImageUrls(final List<ImageSrc> imageSrcs, final String path) {
                return Observable.create(new Observable.OnSubscribe<List<ImageUrl>>() {
                        @Override
                        public void call(Subscriber<? super List<ImageUrl>> subscriber) {
                                List<ImageUrl> imageUrls = new ArrayList<>();
                                for (int i = 0; i < imageSrcs.size(); i++) {
                                        ImageSrc imageSrc = imageSrcs.get(i);
                                        ImageUrl imageUrl = new ImageUrl(imageSrc.getImageurlid(),imageSrc.getSrc(),false);
                                        imageUrl.setChapter(path);
                                        imageUrls.add(imageUrl);
                                }
                                subscriber.onNext(imageUrls);
                                subscriber.onCompleted();
                        }
                }).subscribeOn(Schedulers.io());
        }

        public void insertImages(Long comicid, List<ImageUrl> list) {
                if (list != null && list.size() > 0) {
                        List<ImageSrc> imageSrcs = new ArrayList<>();
                        for (int i = 0; i < list.size(); i++) {
                                imageSrcs.add(new ImageSrc(comicid, list.get(i).getUrl()));
                        }
                        mImageSrcDao.insertInTx(imageSrcs);
                }

        }
}