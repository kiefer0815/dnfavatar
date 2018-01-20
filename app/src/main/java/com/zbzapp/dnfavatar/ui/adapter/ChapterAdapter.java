package com.zbzapp.dnfavatar.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.misc.Pair;
import com.zbzapp.dnfavatar.model.Chapter;
import com.zbzapp.dnfavatar.ui.widget.ChapterButton;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/11/15.
 */

public class ChapterAdapter extends BaseAdapter<Pair<Chapter, Boolean>> {

    private static final int TYPE_ITEM = 2017030222;
    private static final int TYPE_BUTTON = 2017030223;

    private boolean isButtonMode = false;

    static class ItemHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.item_select_title) TextView chapterTitle;
        @BindView(R.id.item_select_checkbox) CheckBox chapterChoice;

        ItemHolder(View view) {
            super(view);
        }
    }

    static class ButtonHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.item_chapter_button) ChapterButton chapterButton;

        ButtonHolder(View view) {
            super(view);
        }
    }

    public ChapterAdapter(Context context, List<Pair<Chapter, Boolean>> list) {
        super(context, list);
    }

    @Override
    public int getItemViewType(int position) {
        return isButtonMode ? TYPE_BUTTON : TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = mInflater.inflate(R.layout.item_select, parent, false);
            return new ItemHolder(view);
        }
        View view = mInflater.inflate(R.layout.item_chapter, parent, false);
        return new ButtonHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final Pair<Chapter, Boolean> pair = mDataSet.get(position);
        if (isButtonMode) {
            final ButtonHolder viewHolder = (ButtonHolder) holder;
            viewHolder.chapterButton.setText(pair.first.getTitle());
            if (pair.first.isDownload()) {
                viewHolder.chapterButton.setDownload(true);
                viewHolder.chapterButton.setSelected(false);
            } else {
                viewHolder.chapterButton.setDownload(false);
                viewHolder.chapterButton.setSelected(pair.second);
            }
        } else {
            ItemHolder viewHolder = (ItemHolder) holder;
            viewHolder.chapterTitle.setText(pair.first.getTitle());
            viewHolder.chapterChoice.setEnabled(!pair.first.isDownload());
            viewHolder.chapterChoice.setChecked(pair.second);
        }
    }

    public void setButtonMode(boolean enable) {
        isButtonMode = enable;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offset = parent.getWidth() / 40;
                outRect.set(offset, 0, offset, (int) (offset * 1.5));
            }
        };
    }

    @Override
    protected boolean isClickValid() {
        return true;
    }

}
