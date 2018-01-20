package com.zbzapp.dnfavatar.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.xiaochen.progressroundbutton.AnimDownloadProgressButton;
import com.zbzapp.dnfavatar.BuildConfig;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.model.Recommend;
import com.zbzapp.dnfavatar.utils.PerfectClickListener;

import java.io.File;
import java.util.List;

/**
 * Created by kiefer on 2017/10/20.
 */

public class RecommendAdapter extends BaseAdapter<Recommend> {

        public RecommendAdapter(Context context, List<Recommend> list) {
                super(context, list);
        }

        @Override
        public RecyclerView.ItemDecoration getItemDecoration() {
                return new RecyclerView.ItemDecoration() {
                        @Override
                        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                RecyclerView.State state) {
                                int offset = parent.getWidth() / 90;
                                outRect.set(offset, 0, offset, (int) (2.8 * offset));
                        }
                };
        }

        static class ListHolder extends BaseViewHolder {

                @BindView(R.id.tv_title)
                TextView comicTitle;
                @BindView(R.id.tv_intro)
                TextView comicIntro;
                @BindView(R.id.item_simpledraweeview)
                SimpleDraweeView simpleDraweeView;
                @BindView(R.id.anim_btn)
                AnimDownloadProgressButton animBtn;
                ListHolder(View view) {
                        super(view);
                }
        }



        @Override
        public RecommendAdapter.ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = mInflater.inflate(R.layout.recommend_item, parent, false);
                return new RecommendAdapter.ListHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                final Recommend item = mDataSet.get(position);
                final RecommendAdapter.ListHolder listHolder = (RecommendAdapter.ListHolder) holder;
                listHolder.comicTitle.setText(item.getTitle());
                listHolder.comicIntro.setText(item.getIntro());
                listHolder.simpleDraweeView.setImageURI(Uri.parse(item.getPic()));
                listHolder.animBtn.setCurrentText("下载");
                listHolder.animBtn.setOnClickListener(new PerfectClickListener() {
                        @Override
                        protected void onNoDoubleClick(View v) {
                                FileDownloader.getImpl().create(item.getDownload_url())
                                        .setPath(FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "download"
                                                + File.separator+item.getTitle() +".apk")
                                        .setListener(new FileDownloadSampleListener(){
                                                @Override
                                                protected void started(BaseDownloadTask task) {
                                                        super.started(task);
                                                        listHolder.animBtn.setCurrentText("下载中");
                                                }

                                                @Override
                                                protected void progress(BaseDownloadTask task,
                                                        int soFarBytes, int totalBytes) {
                                                        super.progress(task, soFarBytes, totalBytes);
                                                        float num= (float)soFarBytes/totalBytes;
                                                        listHolder.animBtn.setState(
                                                                AnimDownloadProgressButton.DOWNLOADING);
                                                        listHolder.animBtn.setProgressText("" , (num*100));
                                                }

                                                @Override
                                                protected void error(BaseDownloadTask task,
                                                        Throwable e) {
                                                        super.error(task, e);
                                                        listHolder.animBtn.setCurrentText("下载");
                                                        listHolder.animBtn.setState(AnimDownloadProgressButton.NORMAL);
                                                }

                                                @Override
                                                protected void completed(BaseDownloadTask task) {
                                                        super.completed(task);
                                                        listHolder.animBtn.setCurrentText("下载完成");
                                                        listHolder.animBtn.setState(AnimDownloadProgressButton.NORMAL);
                                                        installApk(new File(task.getTargetFilePath()));
                                                }
                                        }).start();

                        }
                });

        }
        /**
         * 安装软件
         *
         * @param apkFile
         */
        private void installApk(File apkFile) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                //判断是否是AndroidN以及更高的版本
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri contentUri = FileProvider
                                .getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
                        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                // 执行意图进行安装
                mContext.startActivity(intent);
        }

}
