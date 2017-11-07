package top.soyask.calendarii.ui.adapter.month;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.ui.fragment.month.MonthFragment;

/**
 * Created by mxf on 2017/8/8.
 */
public class MonthFragmentAdapter extends FragmentPagerAdapter {

    public static final int YEAR_END = Global.YEAR_END;
    public static final int YEAR_START = Global.YEAR_START_REAL;
    //1970-2200
    private MonthFragment.OnDaySelectListener mOnDaySelectListener;

    public MonthFragmentAdapter(FragmentManager fm, MonthFragment.OnDaySelectListener onDaySelectListener) {
        super(fm);
        this.mOnDaySelectListener = onDaySelectListener;
    }

    @Override
    public Fragment getItem(int position) {
        MonthFragment monthFragment = MonthFragment.newInstance(position);
        monthFragment.setOnDaySelectListener(mOnDaySelectListener);
        return monthFragment;
    }

    @Override
    public long getItemId(int position) {
        return position + mOnDaySelectListener.hashCode();
    }

    @Override
    public int getCount() {
        return (YEAR_END - YEAR_START + 1) * Global.MONTH_COUNT;
    }
}
