package com.zbzapp.dnfavatar.ui.view;

import com.zbzapp.dnfavatar.component.DialogCaller;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface SettingsView extends BaseView, DialogCaller {

    void onFileMoveSuccess();

    void onExecuteSuccess();

    void onExecuteFail();

}
