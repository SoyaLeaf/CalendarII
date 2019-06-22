package top.soyask.calendarii.ui.fragment.list;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import top.soyask.calendarii.R;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.memorial.AllMemorialFragment;
import top.soyask.calendarii.ui.fragment.thing.AllThingsFragment;
import top.soyask.calendarii.utils.TabLayoutMediator;

public class AllListFragment extends BaseFragment {

    private Toolbar mToolbar;

    public static AllListFragment newInstance() {
        Bundle args = new Bundle();
        AllListFragment fragment = new AllListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public AllListFragment() {
        super(R.layout.fragment_all_list);
    }

    @Override
    protected void setupUI() {
        mToolbar = findToolbar();
        mToolbar.setTitle(R.string.thing);
        mToolbar.setNavigationOnClickListener(v -> removeFragment(this));
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager2 viewPager = findViewById(R.id.vp);
        viewPager.setAdapter(getAdapter());
        viewPager.registerOnPageChangeCallback(getPageChangeCallback());
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.ic_add_thing);
                    break;
                case 1:
                    tab.setIcon(R.drawable.ic_add_memorial_day);
                    break;
            }
        }).attach();
    }

    private ViewPager2.OnPageChangeCallback getPageChangeCallback() {
        return new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mToolbar.setTitle(R.string.thing);
                } else if (position == 1) {
                    mToolbar.setTitle(R.string.memorial_day);
                }
            }
        };
    }

    private RecyclerView.Adapter getAdapter() {
        return new FragmentStateAdapter(getFragmentManager()) {
            @Override
            public int getItemCount() {
                return 2;
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return AllThingsFragment.newInstance();
                } else {
                    return AllMemorialFragment.newInstance();
                }
            }
        };
    }
}
