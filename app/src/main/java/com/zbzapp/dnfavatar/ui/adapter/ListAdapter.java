package com.zbzapp.dnfavatar.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.manager.SourceManager;
import com.zbzapp.dnfavatar.model.MiniComic;

import java.util.List;

/**
 * Created by kiefer on 2017/9/13.
 */

public class ListAdapter extends BaseAdapter<MiniComic> {

        public static int TYPE_LIST = 20171010;

        private SourceManager.TitleGetter mTitleGetter;
        private boolean symbol = false;

        static class ListHolder extends BaseViewHolder {

                @BindView(R.id.comic_title)
                TextView comicTitle;

                ListHolder(View view) {
                        super(view);
                }
        }

        public ListAdapter(Context context, List<MiniComic> list) {
                super(context, list);
        }

        @Override
        public int getItemViewType(int position) {
                return TYPE_LIST;
        }

        @Override
        public ListAdapter.ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = mInflater.inflate(R.layout.item_list, parent, false);
                return new ListAdapter.ListHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                MiniComic comic = mDataSet.get(position);
                ListAdapter.ListHolder gridHolder = (ListAdapter.ListHolder) holder;
                gridHolder.comicTitle.setText(comic.getTitle());

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
                for (MiniComic comic : mDataSet) {
                        if (id == comic.getId()) {
                                remove(comic);
                                break;
                        }
                }
        }

        public int findFirstNotHighlight() {
                int count = 0;
                if (symbol) {
                        for (MiniComic comic : mDataSet) {
                                if (!comic.isHighlight()) {
                                        break;
                                }
                                ++count;
                        }
                }
                return count;
        }

        public void cancelAllHighlight() {
                int count = 0;
                for (MiniComic comic : mDataSet) {
                        if (!comic.isHighlight()) {
                                break;
                        }
                        ++count;
                        comic.setHighlight(false);
                }
                notifyItemRangeChanged(0, count);
        }

        public void moveItemTop(MiniComic comic) {
                if (remove(comic)) {
                        add(findFirstNotHighlight(), comic);
                }
        }

}