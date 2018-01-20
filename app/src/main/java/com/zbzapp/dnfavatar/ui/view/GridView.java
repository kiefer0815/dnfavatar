package com.zbzapp.dnfavatar.ui.view;

import com.zbzapp.dnfavatar.component.DialogCaller;
import com.zbzapp.dnfavatar.component.ThemeResponsive;
import com.zbzapp.dnfavatar.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/9/30.
 */

public interface GridView extends BaseView, DialogCaller, ThemeResponsive {

    void onComicLoadSuccess(List<MiniComic> list);

    void onComicLoadFail();

    void onExecuteFail();

}
