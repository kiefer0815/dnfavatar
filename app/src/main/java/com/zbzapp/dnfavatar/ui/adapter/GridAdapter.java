package com.zbzapp.dnfavatar.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.zbzapp.dnfavatar.App;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.fresco.ControllerBuilderProvider;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.model.MiniComic;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class GridAdapter extends BaseAdapter<Object> {

    public static int TYPE_GRID = 2016101213;

    static final int TYPE_DATA = 0;
    static final int TYPE_AD = 1;


    private ControllerBuilderProvider mProvider;
    private SourceManager.TitleGetter mTitleGetter;
    private boolean symbol = false;

    private HashMap<NativeExpressADView, Integer> mAdViewPositionMap;


    class GridHolder extends BaseViewHolder {

        public TextView comicTitle;
        public View comicHighlight;
        public TextView comicUpdate;
        public TextView comicSource;
        public SimpleDraweeView comicImage;
        public FrameLayout container;

        GridHolder(View view) {
            super(view);
            comicTitle = (TextView) view.findViewById(R.id.item_grid_title);
            comicSource = (TextView) view.findViewById(R.id.item_grid_subtitle);
            comicHighlight = view.findViewById(R.id.item_grid_symbol);
            comicImage = (SimpleDraweeView) view.findViewById(R.id.item_grid_image);
            container = (FrameLayout) view.findViewById(R.id.express_ad_container);
        }
    }

    public GridAdapter(Context context, List<Object> list) {
        super(context, list);
    }

    public GridAdapter(Context context, List<Object> list,HashMap<NativeExpressADView, Integer> map) {
        super(context, list);
        mAdViewPositionMap = map;
    }


    @Override
    public GridHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = (viewType == TYPE_AD) ? R.layout.item_ad_grid : R.layout.item_grid;
        View view = mInflater.inflate(layoutId, parent, false);
        GridAdapter.GridHolder viewHolder = new  GridAdapter.GridHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        int type = getItemViewType(position);
        GridHolder gridHolder = (GridHolder) holder;
        if (TYPE_AD == type) {
            final NativeExpressADView adView = (NativeExpressADView) mDataSet.get(position);
            mAdViewPositionMap.put(adView, position); // 广告在列表中的位置是可以被更新的
            if (gridHolder.container.getChildCount() > 0
                    && gridHolder.container.getChildAt(0) == adView) {
                return;
            }

            if (gridHolder.container.getChildCount() > 0) {
                gridHolder.container.removeAllViews();
            }

            if (adView.getParent() != null) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }

            gridHolder.container.addView(adView);
            adView.render(); // 调用render方法后sdk才会开始展示广告
        } else {
            MiniComic comic = (MiniComic) mDataSet.get(position);

            gridHolder.comicTitle.setText(comic.getTitle());
            gridHolder.comicSource.setText(mTitleGetter.getTitle(comic.getSource()));
            if (mProvider != null) {
                ImageRequest request = ImageRequestBuilder
                        .newBuilderWithSource(Uri.parse(comic.getCover()))
                        .setResizeOptions(new ResizeOptions(App.mCoverWidthPixels / 3, App.mCoverHeightPixels / 3))
                        .build();
                DraweeController controller = mProvider.get(comic.getSource())
                        .setOldController(gridHolder.comicImage.getController())
                        .setImageRequest(request)
                        .build();
                gridHolder.comicImage.setController(controller);
            }
            gridHolder.comicHighlight.setVisibility(symbol && comic.isHighlight() ? View.VISIBLE : View.INVISIBLE);
        }



    }

    public void setProvider(ControllerBuilderProvider provider) {
        mProvider = provider;
    }

    public void setTitleGetter(SourceManager.TitleGetter getter) {
        mTitleGetter = getter;
    }

    public void setSymbol(boolean symbol) {
        this.symbol = symbol;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offset = parent.getWidth() / 90;
                outRect.set(offset, 0, offset, (int) (2.8 * offset));
            }
        };
    }

    public void removeItemById(long id) {
        for (Object comic : mDataSet) {
            if (comic instanceof MiniComic) {
                MiniComic item = (MiniComic) comic;
                if( id == item.getId()){
                    remove(comic);
                    break;
                }

            }
        }
    }

    public int findFirstNotHighlight() {
        int count = 0;
        if (symbol) {
            for (Object comic : mDataSet) {
                if (comic instanceof MiniComic) {
                    MiniComic item = (MiniComic) comic;
                    if (!item.isHighlight()) {
                        break;
                    }
                    ++count;
                }

            }
        }
        return count;
    }

    public void cancelAllHighlight() {
        int count = 0;
        for (Object comic : mDataSet) {
            if (comic instanceof MiniComic) {
                MiniComic item = (MiniComic) comic;
                if (!item.isHighlight()) {
                    break;
                }
                ++count;
                item.setHighlight(false);
            }

        }
        notifyItemRangeChanged(0, count);
    }

    public void moveItemTop(MiniComic comic) {
        if (remove(comic)) {
            add(findFirstNotHighlight(), comic);
        }
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
