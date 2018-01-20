package com.zbzapp.dnfavatar.ui.view;

import com.zbzapp.dnfavatar.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface HistoryView extends GridView {

    void onHistoryDelete(long id);

    void onItemUpdate(MiniComic comic);

    void OnComicRestore(List<MiniComic> list);

    void onHistoryClearSuccess();

}
