package com.zbzapp.dnfavatar.helper;

import com.zbzapp.dnfavatar.manager.PreferenceManager;
import com.zbzapp.dnfavatar.model.Comic;
import com.zbzapp.dnfavatar.model.ComicDao;
import com.zbzapp.dnfavatar.model.DaoSession;
import com.zbzapp.dnfavatar.model.Source;
import com.zbzapp.dnfavatar.source.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public class UpdateHelper {

    // 1.04.08.004
    private static final int VERSION = 10408004;

    public static void update(PreferenceManager manager, final DaoSession session) {
        int version = manager.getInt(PreferenceManager.PREF_APP_VERSION, 0);
        if (version != VERSION) {
            switch (version) {
                case 0:
                    initSource(session);
                    break;
                case 100001:
                     break;
                case 100004:
                    break;
                case 10404001:
                case 10404002:
                case 10404003:
                case 10405000:
                    session.getSourceDao().insert(Dmzjv2.getDefaultSource());
                case 10406000:
                case 10407000:
                case 10408000:
                    deleteDownloadFromLocal(session);
                case 10408001:
                case 10408002:
                case 10408003:
                    session.getSourceDao().insert(MangaNel.getDefaultSource());
            }
            manager.putInt(PreferenceManager.PREF_APP_VERSION, VERSION);
        }
    }

    /**
     * app: 1.4.8.0 -> 1.4.8.1
     * 删除本地漫画中 download 字段的值
     */
    private static void deleteDownloadFromLocal(final DaoSession session) {
        session.runInTx(new Runnable() {
            @Override
            public void run() {
                ComicDao dao = session.getComicDao();
                List<Comic> list = dao.queryBuilder().where(ComicDao.Properties.Local.eq(true)).list();
                if (!list.isEmpty()) {
                    for (Comic comic : list) {
                        comic.setDownload(null);
                    }
                    dao.updateInTx(list);
                }
            }
        });
    }

    /**
     * 初始化图源
     */
    private static void initSource(DaoSession session) {
        List<Source> list = new ArrayList<>(11);
        list.add(IKanman.getDefaultSource());
        list.add(Dmzj.getDefaultSource());
        list.add(HHAAZZ.getDefaultSource());
        list.add(CCTuku.getDefaultSource());
        list.add(U17.getDefaultSource());
        list.add(DM5.getDefaultSource());
        list.add(Webtoon.getDefaultSource());
        list.add(HHSSEE.getDefaultSource());
        list.add(MH57.getDefaultSource());
        list.add(Chuiyao.getDefaultSource());
        list.add(Dmzjv2.getDefaultSource());
        list.add(MangaNel.getDefaultSource());

        session.getSourceDao().insertOrReplaceInTx(list);
    }

}
