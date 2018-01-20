package com.zbzapp.dnfavatar.core;

import android.util.SparseBooleanArray;
import com.zbzapp.dnfavatar.App;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.misc.Pair;
import com.zbzapp.dnfavatar.model.Chapter;
import com.zbzapp.dnfavatar.model.Comic;
import com.zbzapp.dnfavatar.model.ImageUrl;
import com.zbzapp.dnfavatar.parser.Parser;
import com.zbzapp.dnfavatar.parser.SearchIterator;
import okhttp3.*;
import org.json.JSONArray;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Hiroshi on 2016/8/20.
 */
public class Manga {

    private static int filterResult(String str, SparseBooleanArray hash) {
        int count = 0;
        if (str != null) {
            for (int i = 0; i < str.length(); ++i) {
                if (hash.get(str.charAt(i), false)) {
                    ++count;
                }
            }
        }
        return count;
    }

    public static Observable<Comic> getSearchResult(final Parser parser, final String keyword,
                                                    final int page, final SparseBooleanArray hash,
                                                    final int limit) {
        return Observable.create(new Observable.OnSubscribe<Comic>() {
            @Override
            public void call(Subscriber<? super Comic> subscriber) {
                try {
                    Request request = parser.getSearchRequest(keyword, page);
                    Random random = new Random();
                    String html = getResponseBody(App.getHttpClient(), request);
                    SearchIterator iterator = parser.getSearchIterator(html, page);
                    if (iterator == null || iterator.empty()) {
                        throw new Exception();
                    }
                    while (iterator.hasNext()) {
                        Comic comic = iterator.next();
                        if (comic != null) {
                            if (hash == null || filterResult(comic.getTitle(), hash) > limit ||
                                    filterResult(comic.getAuthor(), hash) > limit) {
                                subscriber.onNext(comic);
                                Thread.sleep(random.nextInt(200));
                            }
                        }
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<Chapter>> getComicInfo(final Parser parser, final Comic comic) {
        return Observable.create(new Observable.OnSubscribe<List<Chapter>>() {
            @Override
            public void call(Subscriber<? super List<Chapter>> subscriber) {
                try {
                    Request request = parser.getInfoRequest(comic.getCid());
                    String html = getResponseBody(App.getHttpClient(), request);
                    parser.parseInfo(html, comic);
                    request = parser.getChapterRequest(html, comic.getCid());
                    if (request != null) {
                        html = getResponseBody(App.getHttpClient(), request);
                    }
                    List<Chapter> list = parser.parseChapter(html);
                    if (!list.isEmpty()) {
                        subscriber.onNext(list);
                        subscriber.onCompleted();
                    } else {
                        throw new ParseErrorException();
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<Comic>> getCategoryComic(final Parser parser, final String format,
                                                           final int page) {
        return Observable.create(new Observable.OnSubscribe<List<Comic>>() {
            @Override
            public void call(Subscriber<? super List<Comic>> subscriber) {
                try {
                    Request request = parser.getCategoryRequest(format, page,parser.getHeader());
                    String html = getResponseBody(App.getHttpClient(), request);
                    List<Comic> list = parser.parseCategory(html, page);
                    if (!list.isEmpty()) {
                        subscriber.onNext(list);
                        subscriber.onCompleted();
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<ImageUrl>> getChapterImage(final Parser parser, final String cid,
                                                             final String path) {
        return Observable.create(new Observable.OnSubscribe<List<ImageUrl>>() {
            @Override
            public void call(Subscriber<? super List<ImageUrl>> subscriber) {
                String html;
                try {
                    Request request = parser.getImagesRequest(cid, path);
                    html = getResponseBody(App.getHttpClient(), request);
                    List<ImageUrl> list = parser.parseImages(html);
                    if (list.isEmpty()) {
                        throw new Exception();
                    } else {
                        for (ImageUrl imageUrl : list) {
                            imageUrl.setChapter(path);
                        }
                        subscriber.onNext(list);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static List<ImageUrl> getImageUrls(Parser parser, String cid, String path) throws InterruptedIOException {
        List<ImageUrl> list = new ArrayList<>();
        Response response = null;
        try {
            Request request  = parser.getImagesRequest(cid, path);
            response = App.getHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                list.addAll(parser.parseImages(response.body().string()));
            } else {
                throw new NetworkErrorException();
            }
        } catch (InterruptedIOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return list;
    }

    public static String getLazyUrl(Parser parser, String url) throws InterruptedIOException {
        Response response = null;
        try {
            Request request = parser.getLazyRequest(url);
            response = App.getHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                return parser.parseLazy(response.body().string(), url);
            } else {
                throw new NetworkErrorException();
            }
        } catch (InterruptedIOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    public static Observable<String> loadLazyUrl(final Parser parser, final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Request request = parser.getLazyRequest(url);
                String newUrl = null;
                try {
                    newUrl = parser.parseLazy(getResponseBody(App.getHttpClient(), request), url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                subscriber.onNext(newUrl);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<String>> loadAutoComplete(final String keyword) {
        return Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                RequestBody body = new FormBody.Builder()
                        .add("key", keyword)
                        .add("s", "1")
                        .build();
                Request request = new Request.Builder()
                        .url("http://m.ikanman.com/support/word.ashx")
                        .post(body)
                        .build();
                try {
                    String jsonString = getResponseBody(App.getHttpClient(), request);
                    JSONArray array = new JSONArray(jsonString);
                    List<String> list = new ArrayList<>();
                    for (int i = 0; i != array.length(); ++i) {
                        list.add(array.getJSONObject(i).getString("t"));
                    }
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<Pair<Comic, Pair<Integer, Integer>>> checkUpdate(
            final SourceManager manager, final List<Comic> list) {
        return Observable.create(new Observable.OnSubscribe<Pair<Comic, Pair<Integer, Integer>>>() {
            @Override
            public void call(Subscriber<? super Pair<Comic, Pair<Integer, Integer>>> subscriber) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(1500, TimeUnit.MILLISECONDS)
                        .readTimeout(1500, TimeUnit.MILLISECONDS)
                        .build();
                int size = list.size();
                int count = 0;
                for (Comic comic : list) {
                    Pair<Comic, Pair<Integer, Integer>> pair = Pair.create(comic, Pair.create(++count, size));
                    if (comic.getSource() < 100) {
                        Parser parser = manager.getParser(comic.getSource());
                        Request request = parser.getCheckRequest(comic.getCid());
                        try {
                            String update = parser.parseCheck(getResponseBody(client, request));
                            if (comic.getUpdate() != null && update != null && !comic.getUpdate().equals(update)) {
                                comic.setFavorite(System.currentTimeMillis());
                                comic.setUpdate(update);
                                comic.setHighlight(true);
                                subscriber.onNext(pair);
                                continue;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    pair.first = null;
                    subscriber.onNext(pair);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static String getResponseBody(OkHttpClient client, Request request) throws NetworkErrorException {
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                byte[] b = response.body().bytes(); //获取数据的bytes
                String info = new String(b, "GB2312"); //然后将其转为gb2312
                return info;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        throw new NetworkErrorException();
    }

    public static String getUTF8ResponseBody(OkHttpClient client, Request request) throws NetworkErrorException {
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                //                byte[] b = response.body().bytes(); //获取数据的bytes
                //                String info = new String(b, "GB2312"); //然后将其转为gb2312
                return response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        throw new NetworkErrorException();
    }

    public static class ParseErrorException extends Exception {}

    public static class NetworkErrorException extends Exception {}




}
