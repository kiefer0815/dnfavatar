package com.zbzapp.dnfavatar.ui.view;

import com.zbzapp.dnfavatar.component.DialogCaller;
import com.zbzapp.dnfavatar.component.ThemeResponsive;
import com.zbzapp.dnfavatar.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface ComicView extends BaseView, ThemeResponsive, DialogCaller {

    void onTagLoadSuccess(List<Tag> list);

    void onTagLoadFail();

}
