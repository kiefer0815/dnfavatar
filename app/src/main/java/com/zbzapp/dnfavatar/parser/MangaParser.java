package com.zbzapp.dnfavatar.parser;

import com.zbzapp.dnfavatar.model.Comic;
import com.zbzapp.dnfavatar.model.Source;
import com.zbzapp.dnfavatar.utils.StringUtils;
import okhttp3.Headers;
import okhttp3.Request;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/22.
 */
public abstract class MangaParser implements Parser {

    protected String mTitle;
    private Category mCategory;

    protected void init(Source source, Category category) {
        mTitle = source.getTitle();
        mCategory = category;
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        return null;
    }

    @Override
    public Request getLazyRequest(String url) {
        return null;
    }

    @Override
    public String parseLazy(String html, String url) {
        return null;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return null;
    }

    @Override
    public String parseCheck(String html) {
        return null;
    }

    @Override
    public Category getCategory() {
        return mCategory;
    }

    @Override
    public Request getCategoryRequest(String format, int page) {
        String url = StringUtils.format(format, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public Request getCategoryRequest(String format, int page, Headers headers) {
        String url = StringUtils.format(format, page);
        return new Request.Builder().url(url).headers(headers).build();
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        return null;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    protected String[] buildUrl(String path, String[] servers) {
        if (servers != null) {
            String[] url = new String[servers.length];
            for (int i = 0; i != servers.length; ++i) {
                url[i] = servers[i].concat(path);
            }
            return url;
        }
        return null;
    }

    protected boolean isFinish(String text) {
        return text != null && (text.contains("完结") || text.contains("Completed")) ;
    }

}
