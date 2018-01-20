package com.zbzapp.dnfavatar.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.zbzapp.dnfavatar.App;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.fresco.ControllerBuilderProvider;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.model.Comic;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class ResultAdapter extends BaseAdapter<Object> {
    static final int TYPE_DATA = 0;
    static final int TYPE_AD = 1;

    private ControllerBuilderProvider mProvider;
    private SourceManager.TitleGetter mTitleGetter;
    private HashMap<NativeExpressADView, Integer> mAdViewPositionMap;


    class ResultViewHolder extends BaseViewHolder {

        public TextView comicTitle;
        public TextView comicAuthor;
        public TextView comicUpdate;
        public TextView comicSource;
        public SimpleDraweeView comicImage;
        public FrameLayout container;

        ResultViewHolder(View view) {
            super(view);
            comicTitle = (TextView) view.findViewById(R.id.result_comic_title);
            comicAuthor = (TextView) view.findViewById(R.id.result_comic_author);
            comicUpdate = (TextView) view.findViewById(R.id.result_comic_update);
            comicSource = (TextView) view.findViewById(R.id.result_comic_source);
            comicImage = (SimpleDraweeView) view.findViewById(R.id.result_comic_image);
            container = (FrameLayout) view.findViewById(R.id.express_ad_container);
        }
    }

    public ResultAdapter(Context context, List<Object> list) {
        super(context, list);
    }

    public ResultAdapter(Context context, List<Object> list,HashMap<NativeExpressADView, Integer> map) {
        super(context, list);
        mAdViewPositionMap = map;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = (viewType == TYPE_AD) ? R.layout.result_ad_item : R.layout.item_result;
        View view = mInflater.inflate(layoutId, parent, false);
        ResultAdapter.ResultViewHolder viewHolder = new ResultAdapter.ResultViewHolder(view);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        int type = getItemViewType(position);
        ResultAdapter.ResultViewHolder viewHolder = (ResultAdapter.ResultViewHolder ) holder;
        if (TYPE_AD == type) {
            final NativeExpressADView adView = (NativeExpressADView) mDataSet.get(position);
            mAdViewPositionMap.put(adView, position); // 广告在列表中的位置是可以被更新的
            if (viewHolder.container.getChildCount() > 0
                    && viewHolder.container.getChildAt(0) == adView) {
                return;
            }

            if (viewHolder.container.getChildCount() > 0) {
                viewHolder.container.removeAllViews();
            }

            if (adView.getParent() != null) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }

            viewHolder.container.addView(adView);
            adView.render(); // 调用render方法后sdk才会开始展示广告
        } else {
            Comic comic = (Comic) mDataSet.get(position);

            viewHolder.comicTitle.setText(comic.getTitle());
            viewHolder.comicAuthor.setText(comic.getAuthor());
            viewHolder.comicSource.setText(mTitleGetter.getTitle(comic.getSource()));
            viewHolder.comicUpdate.setText(comic.getUpdate());
            ImageRequest request = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(comic.getCover()))
                    .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                    .build();
            viewHolder.comicImage.setController(mProvider.get(comic.getSource()).setImageRequest(request).build());
        }

    }

    public void setProvider(ControllerBuilderProvider provider) {
        mProvider = provider;
    }

    public void setTitleGetter(SourceManager.TitleGetter getter) {
        mTitleGetter = getter;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offset = parent.getWidth() / 90;
                outRect.set(0, 0, 0, offset);
            }
        };
    }
    @Override
    public int getItemViewType(int position) {
        return mDataSet.get(position) instanceof NativeExpressADView ? TYPE_AD : TYPE_DATA;
    }


    // 把返回的NativeExpressADView添加到数据集里面去
    public void addADViewToPosition(int position, NativeExpressADView adView) {
        if (position >= 0 && position < mDataSet.size() && adView != null) {
            mDataSet.add(position, adView);
        }
    }

    // 移除NativeExpressADView的时候是一条一条移除的
    public void removeADView(int position, NativeExpressADView adView) {
        mDataSet.remove(position);
        notifyItemRemoved(position); // position为adView在当前列表中的位置
        notifyItemRangeChanged(0, mDataSet.size() - 1);
    }

}
