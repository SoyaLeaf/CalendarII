package top.soyask.calendarii.ui.fragment.base;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import top.soyask.calendarii.R;

public abstract class BaseListFragment extends BaseFragment {

    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView mRecyclerView;

    public BaseListFragment() {
        super(R.layout.fragment_base_list);
    }

    @Override
    protected void setupUI() {
        init();
        mRecyclerView = mContentView.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mHostActivity, RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = getAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(getScrollListener(layoutManager));
    }

    protected abstract void init();

    private RecyclerView.OnScrollListener getScrollListener(LinearLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int position = layoutManager.findLastVisibleItemPosition();
                if (position == mAdapter.getItemCount() - 1 && canLoadMore()) {
                    loadData();
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    protected void scrollToTop() {
        mRecyclerView.scrollToPosition(0);
    }

    protected abstract boolean canLoadMore();

    protected abstract void loadData();

    protected abstract RecyclerView.Adapter getAdapter();
}
