
package com.zbzapp.dnfavatar.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.zbzapp.dnfavatar.App;
import com.zbzapp.dnfavatar.constant.Constants;
import com.zbzapp.dnfavatar.ui.activity.LockScreenActivity;
import com.zbzapp.dnfavatar.ui.activity.OldMainActivity;

import java.sql.SQLException;

/**
 * User: zhaohaifeng
 * Date: 14-10-13
 * Time: 下午10:27
 */
public class UserManager {

    public static long INVALID_ID = -1;
    private static UserManager userManager;

    private Context context;

    private static SharedPreferences mSettingStore = App.applicationContext.getSharedPreferences("SettingStore", 0);

    public UserManager()  {
        this.context = App.applicationContext;
        try {
            init();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static UserManager uniqueInstance() {
        UserManager result = userManager;
        if (result == null) {
            synchronized (UserManager.class) {
                result = userManager;
                if (result == null)
                    result = userManager = new UserManager();

            }
        }

        return result;
    }

    public void init() throws SQLException {

    }


    public static void gotoFirstPage(Context mContext){
        if(TextUtils.isEmpty(UserManager.getExtraInfo(Constants.LOCK_SCREEN_STRING,""))){
            OldMainActivity.start(mContext);
        }else {
            LockScreenActivity.start(mContext,"2");
        }
    }
    /**
     * 获取用户配置信息
     *
     * @param key
     * @return
     */
    public static String getExtraInfo(String key) {
        String value = mSettingStore.getString(key, null);
        return value;
    }

    /**
     * 获取用户配置信息
     *
     * @param key
     * @return
     */
    public static String getExtraInfo(String key, String defaultValue) {
        String value = mSettingStore.getString(key, null);
        if (value == null || "".equals(value)) {
            return defaultValue;
        }
        return value;
    }


    public static boolean setExtraInfo(String key, String value) {
        return mSettingStore.edit().putString(key, value).commit();
    }

}
