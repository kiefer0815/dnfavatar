package com.zbzapp.dnfavatar.presenter;

import com.zbzapp.dnfavatar.manager.ComicManager;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.model.Source;
import com.zbzapp.dnfavatar.ui.view.SourceDetailView;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public class SourceDetailPresenter extends BasePresenter<SourceDetailView> {

    private SourceManager mSourceManager;
    private ComicManager mComicManager;

    @Override
    protected void onViewAttach() {
        mSourceManager = SourceManager.getInstance(mBaseView);
        mComicManager = ComicManager.getInstance(mBaseView);
    }

    public void load(int type) {
        Source source = mSourceManager.load(type);
        long count = mComicManager.countBySource(type);
        mBaseView.onSourceLoadSuccess(type, source.getTitle(), count);
    }

}
