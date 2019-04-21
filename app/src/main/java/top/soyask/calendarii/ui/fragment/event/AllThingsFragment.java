package top.soyask.calendarii.ui.fragment.event;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.ThingDao;
import top.soyask.calendarii.entity.Thing;
import top.soyask.calendarii.ui.adapter.thing.ThingAdapter;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;


public class AllThingsFragment extends BaseFragment
        implements ThingAdapter.ThingActionCallback, View.OnClickListener, DeleteFragment.OnDeleteConfirmListener {

    private static final int WAIT = 0x0;
    private static final int CANCEL = 0x1;
    private static final int DELETE_ALL = 0x2;
    private static final int DELETE_COMP = 0x3;
    private static final String TITLE = "TITLE";
    private ThingAdapter mThingAdapter;
    private ThingDao mThingDao;
    private List<Thing> mThings = new ArrayList<>();
    private List<Thing> mDoneThings = new ArrayList<>();
    private ProgressDialog mProgressDialog;

    private Handler mHandler = new ThingHandler(this);


    private Comparator<Thing> mComparator = (o1, o2) -> {
        long time1 = o1.getTargetTime();
        long time2 = o2.getTargetTime();
        return -Long.compare(time1, time2);
    };
    private RecyclerView mRecyclerView;
    private int currentPage = 0;
    private int mCount;

    public AllThingsFragment() {
        super(R.layout.fragment_all);
    }

    public static AllThingsFragment newInstance(String title) {
        AllThingsFragment fragment = new AllThingsFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        mThingDao = ThingDao.getInstance(mHostActivity);
        mCount = mThingDao.count();
        loadData();
        mRecyclerView = findViewById(R.id.rv_event);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mThingAdapter = new ThingAdapter(mThings, this);
        mRecyclerView.setAdapter(mThingAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = layoutManager.findLastVisibleItemPosition();
                if (position == mThingAdapter.getItemCount() - 1 && mThingAdapter.getItemCount() < mCount) {
                    loadData();
                }
            }
        });
        findToolbar().setNavigationOnClickListener(this);
        findViewById(R.id.ib_delete_all).setOnClickListener(this);
        if (mThings.isEmpty()) {
            findViewById(R.id.ib_delete_all).setVisibility(View.GONE);
        }
    }

    private void loadData() {
        currentPage++;
        List<Thing> things = mThingDao.list(currentPage);
        Collections.sort(mThings, mComparator);
        for (Thing thing : mThings) {
            if (thing.isDone()) {
                mDoneThings.add(thing);
            }
        }
        mThings.addAll(things);
    }


    @Override
    public void onEditClick(final int position, Thing thing) {
        EditThingFragment editThingFragment = EditThingFragment.newInstance(null, thing);
        editThingFragment.setOnUpdateListener(() -> mThingAdapter.notifyItemChanged(position));
        editThingFragment.setOnDeleteListener(() -> {
            mThings.remove(position);
            mThingAdapter.notifyItemRemoved(position);
        });
        addFragment(editThingFragment);
    }

    @Override
    public void onDeleteClick(final int position, final Thing thing) {
        mThingDao.delete(thing);
        mThings.remove(thing);
        mThingAdapter.notifyItemRemoved(position);
        mThingAdapter.notifyItemRangeChanged(position, mThings.size());
        showSnackbar("删除成功^_~", "撤销", v -> {
            mThingDao.insert(thing);
            mThings.add(thing);  // FIXME: 2017/8/26 这里不能 thing id变化了
            Collections.sort(mThings, mComparator);
            mThingAdapter.notifyItemInserted(position);
            mThingAdapter.notifyItemRangeChanged(position, mThings.size());
            if (position == 0) {
                mRecyclerView.scrollToPosition(0);
            }
        });
    }

    @Override
    public void onDone(int position, Thing thing) {
        thing.setDone(true);
        mThingDao.update(thing);
        mThingAdapter.notifyItemChanged(position);
        mDoneThings.add(thing);
    }

    @Override
    public void onDoneCancel(int position, Thing thing) {
        thing.setDone(false);
        mThingDao.update(thing);
        mThingAdapter.notifyItemChanged(position);
        if (mDoneThings.contains(thing)) {
            mDoneThings.remove(thing);
        }
    }

    @Override
    public void onShare(Thing event) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        String text = String.format("%s", event.getDetail());
        intent.putExtra(Intent.EXTRA_SUBJECT, ".");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setType("text/plain");
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_delete_all:
                DeleteFragment deleteFragment = DeleteFragment.newInstance();
                deleteFragment.setOnDeleteConfirmListener(this);
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.main, deleteFragment)
                        .addToBackStack(deleteFragment.getClass().getSimpleName())
                        .commit();
                break;
            default:
                removeFragment(this);
                break;
        }
    }

    @Override
    public void onConfirm(int type) {
        if (type == DeleteFragment.ALL) {
            mHandler.sendEmptyMessage(DELETE_ALL);
        } else {
            mHandler.sendEmptyMessage(DELETE_COMP);
        }
    }

    private void deleteComplete() {
        final List<Thing> temp = new ArrayList<>(mDoneThings);
        mThings.removeAll(mDoneThings);
        mThingAdapter.notifyDataSetChanged();
        mThingDao.deleteDone();
        mDoneThings.clear();
        showSnackbar("删除了划掉的事件。", "我要恢复", v -> {
            mHandler.sendEmptyMessage(WAIT);
            for (Thing thing : temp) {
                mThingDao.insert(thing);
            }
            mThings.addAll(temp);
            mDoneThings.addAll(temp);
            Collections.sort(mThings, mComparator);
            mThingAdapter.notifyDataSetChanged();
            mHandler.sendEmptyMessage(CANCEL);
        });
    }

    private void deleteAll() {
        final List<Thing> temp = new ArrayList<>(mThings);
        mThings.clear();
        mThingAdapter.notifyDataSetChanged();
        mThingDao.deleteAll();
        showSnackbar("删除了全部的事件。", "我要恢复", v -> {
            mHandler.sendEmptyMessage(WAIT);
            for (Thing thing : temp) {
                mThingDao.insert(thing);
            }
            mThings.addAll(temp);
            Collections.sort(mThings, mComparator);
            mThingAdapter.notifyDataSetChanged();
            mHandler.sendEmptyMessage(CANCEL);
        });
    }

    private static class ThingHandler extends Handler {
        private final WeakReference<AllThingsFragment> mFragment;

        private ThingHandler(AllThingsFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            AllThingsFragment fragment = mFragment.get();
            if (fragment == null) {
                return;
            }
            switch (msg.what) {
                case WAIT:
                    fragment.mProgressDialog = ProgressDialog.show(fragment.mHostActivity, null, "正在恢复，请稍等...");
                    break;
                case CANCEL:
                    if (fragment.mProgressDialog != null) {
                        fragment.mProgressDialog.dismiss();
                        fragment.mProgressDialog = null;
                    }
                    break;
                case DELETE_ALL:
                    fragment.deleteAll();
                    break;
                case DELETE_COMP:
                    fragment.deleteComplete();
                    break;
            }
        }
    }
}
