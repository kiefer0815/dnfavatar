package com.zbzapp.dnfavatar.ui.view;

import com.zbzapp.dnfavatar.misc.Pair;
import com.zbzapp.dnfavatar.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public interface TagEditorView extends BaseView {

    void onTagLoadSuccess(List<Pair<Tag, Boolean>> list);

    void onTagLoadFail();

    void onTagUpdateSuccess();

    void onTagUpdateFail();

}
