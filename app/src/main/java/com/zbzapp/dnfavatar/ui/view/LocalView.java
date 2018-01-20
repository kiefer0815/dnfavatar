package com.zbzapp.dnfavatar.ui.view;

import com.zbzapp.dnfavatar.component.DialogCaller;
import com.zbzapp.dnfavatar.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2017/5/14.
 */

public interface LocalView extends GridView, DialogCaller {

    void onLocalDeleteSuccess(long id);

    void onLocalScanSuccess(List<MiniComic> list);

}
