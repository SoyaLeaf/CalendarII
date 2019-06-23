package top.soyask.calendarii.ui.fragment.memorial;

import android.os.Bundle;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.MemorialDayDao;
import top.soyask.calendarii.entity.MemorialDay;
import top.soyask.calendarii.ui.adapter.memorail.MemorialDayAdapter;
import top.soyask.calendarii.ui.fragment.base.BaseListFragment;

public class AllMemorialFragment extends BaseListFragment implements MemorialDayAdapter.MemorialDayActionListener {

    private List<MemorialDay> mMemorialDays;
    private MemorialDayDao mMemorialDayDao;

    public static AllMemorialFragment newInstance() {
        Bundle args = new Bundle();
        AllMemorialFragment fragment = new AllMemorialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected boolean canLoadMore() {
        return false;
    }

    @Override
    protected void init() {
        mMemorialDayDao = MemorialDayDao.getInstance(mHostActivity);
        loadData();
    }

    @Override
    protected void loadData() {
        mMemorialDays = mMemorialDayDao.list();
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return new MemorialDayAdapter(mMemorialDays, this);
    }

    @Override
    public void onMemorialDayClick(int position, MemorialDay day) {
        MemorialFragment memorialFragment = MemorialFragment.newInstance(day);
        memorialFragment.setOnMemorialDayUpdateListener(() -> mAdapter.notifyItemChanged(position));
        addFragment(memorialFragment);
    }

    @Override
    public boolean onMemorialDayLongClick(int position, MemorialDay day) {
        new AlertDialog
                .Builder(mHostActivity)
                .setItems(new String[]{getString(R.string.delete)}, (dialog, which) -> {
                    int id = day.getId();
                    mMemorialDayDao.delete(id);
                    mMemorialDays.remove(day);
                    mAdapter.notifyItemRemoved(position);
                    mAdapter.notifyItemRangeChanged(0, position);
                }).show();
        return true;
    }
}
