package top.soyask.calendarii.ui.fragment.setting.zoom;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.database.dao.EventDao;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.ui.adapter.month.MonthAdapter;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.utils.DayUtils;
import top.soyask.calendarii.utils.MonthUtils;

public class ZoomFragment extends BaseFragment implements MonthAdapter.OnItemClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = ZoomFragment.class.getSimpleName();

    private List<Day> mDays = new ArrayList<>();
    private EventDao mEventDao;
    private MonthAdapter mMonthAdapter;

    public ZoomFragment() {
        super(R.layout.fragment_zoom);
    }

    public static ZoomFragment newInstance() {
        ZoomFragment fragment = new ZoomFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private synchronized void setupData() {
        mEventDao = EventDao.getInstance(getMainActivity());
        Calendar calendar = Calendar.getInstance();
        int dayCount = DayUtils.getMonthDayCount(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
        mDays.clear();
        for (int i = 0; i < dayCount; i++) {
            calendar.set(Calendar.DAY_OF_MONTH,i + 1);
            Day day = MonthUtils.generateDay(calendar, mEventDao);
            mDays.add(day);
        }
    }

    @Override
    protected void setupUI() {
        setupData();
        Configuration config = getResources().getConfiguration();
        SeekBar seekBar = findViewById(R.id.sb);
        seekBar.setProgress(config.densityDpi);
        seekBar.setOnSeekBarChangeListener(this);
        mMonthAdapter = new MonthAdapter(mDays, this);
        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 7));
        recyclerView.setAdapter(mMonthAdapter);
        recyclerView.setItemAnimator(null);
    }


    @Override
    public void onDayClick(int position, Day day) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(progress < 200){
            progress = 200;
        }
        Resources resources = getResources();
        Configuration newConfig = new Configuration();
        newConfig.setToDefaults();
        newConfig.densityDpi = progress;
        resources.updateConfiguration(newConfig,resources.getDisplayMetrics());
        getMainActivity().recreate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
