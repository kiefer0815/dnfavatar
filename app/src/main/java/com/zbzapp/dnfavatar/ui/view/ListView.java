package com.zbzapp.dnfavatar.ui.view;

import com.zbzapp.dnfavatar.component.DialogCaller;
import com.zbzapp.dnfavatar.component.ThemeResponsive;
import com.zbzapp.dnfavatar.model.MiniComic;

import java.util.List;

/**
 * Created by kiefer on 2017/9/13.
 */


public interface ListView extends BaseView, DialogCaller, ThemeResponsive {

        void onComicLoadSuccess(List<MiniComic> list);

        void onComicLoadFail();

        void onExecuteFail();

}
