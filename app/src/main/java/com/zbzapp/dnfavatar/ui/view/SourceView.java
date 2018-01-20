package com.zbzapp.dnfavatar.ui.view;

import com.zbzapp.dnfavatar.component.ThemeResponsive;
import com.zbzapp.dnfavatar.model.Source;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface SourceView extends BaseView, ThemeResponsive {

    void onSourceLoadSuccess(List<Source> list);

    void onSourceLoadFail();

}
