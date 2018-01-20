package com.zbzapp.dnfavatar.source;

import com.zbzapp.dnfavatar.model.Chapter;
import com.zbzapp.dnfavatar.model.Comic;
import com.zbzapp.dnfavatar.model.ImageUrl;
import com.zbzapp.dnfavatar.parser.MangaParser;
import com.zbzapp.dnfavatar.parser.SearchIterator;

import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2017/5/21.
 */

public class Locality extends MangaParser {

    public static final int TYPE = -2;
    public static final String DEFAULT_TITLE = "本地漫画";

    public Locality() {
        mTitle = DEFAULT_TITLE;
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        return null;
    }

    @Override
    public Request getInfoRequest(String cid) {
        return null;
    }

    @Override
    public void parseInfo(String html, Comic comic) {}

    @Override
    public List<Chapter> parseChapter(String html) {
        return null;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        return null;
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
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
    public List<Comic> parseCategory(String html, int page) {
        return null;
    }

    @Override
    public Headers getHeader() {
        return null;
    }

}
