package com.zbzapp.dnfavatar.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import butterknife.OnClick;
import com.zbzapp.dnfavatar.R;
import com.zbzapp.dnfavatar.global.Extra;
import com.zbzapp.dnfavatar.misc.Pair;
import com.zbzapp.dnfavatar.model.Tag;
import com.zbzapp.dnfavatar.presenter.BasePresenter;
import com.zbzapp.dnfavatar.presenter.TagEditorPresenter;
import com.zbzapp.dnfavatar.ui.adapter.BaseAdapter;
import com.zbzapp.dnfavatar.ui.adapter.TagEditorAdapter;
import com.zbzapp.dnfavatar.ui.view.TagEditorView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class TagEditorActivity extends CoordinatorActivity implements TagEditorView {

    private TagEditorPresenter mPresenter;
    private TagEditorAdapter mTagAdapter;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new TagEditorPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected BaseAdapter initAdapter() {
        mTagAdapter = new TagEditorAdapter(this, new ArrayList<Pair<Tag, Boolean>>());
        return mTagAdapter;
    }

    @Override
    protected void initActionButton() {
        mActionButton.setImageResource(R.drawable.ic_done_white_24dp);
        mActionButton.show();
        hideProgressBar();
    }

    @Override
    protected void initData() {
        long id = getIntent().getLongExtra(Extra.EXTRA_ID, -1);
        mPresenter.load(id);
    }

    @Override
    public void onTagLoadSuccess(List<Pair<Tag, Boolean>> list) {
        hideProgressBar();
        mTagAdapter.addAll(list);
    }

    @Override
    public void onTagLoadFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_data_load_fail);
    }

    @Override
    public void onTagUpdateSuccess() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onTagUpdateFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    public void onItemClick(View view, int position) {
        Pair<Tag, Boolean> pair = mTagAdapter.getItem(position);
        pair.second = !pair.second;
        mTagAdapter.notifyItemChanged(position);
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        showProgressDialog();
        List<Long> list = new ArrayList<>();
        for (Pair<Tag, Boolean> pair : mTagAdapter.getDateSet()) {
            if (pair.second) {
                list.add(pair.first.getId());
            }
        }
        mPresenter.updateRef(list);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.tag_editor);
    }

    public static Intent createIntent(Context context, long id) {
        Intent intent = new Intent(context, TagEditorActivity.class);
        intent.putExtra(Extra.EXTRA_ID, id);
        return intent;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
