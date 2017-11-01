package top.soyask.calendarii.ui.adapter.month;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.domain.Day;
import top.soyask.calendarii.global.Setting;

/**
 * Created by mxf on 2017/8/8.
 */
public class MonthAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static final int VIEW_WEEK = 0; //显示星期
    static final int VIEW_DAY = 1; //显示日子
    static final int VIEW_SELECTED = 3;
    static final int VIEW_TODAY = 4;
    @Deprecated
    static final int VIEW_EVENT = 5;
    static final String[] WEEK_ARRAY = {"日", "一", "二", "三", "四", "五", "六",};

    private List<Day> mDays;
    private OnItemClickListener mOnItemClickListener;
    private int mSelected = -1;
    private int mDateStartPos = 0;
    private int mEndPosition;

    public MonthAdapter(@NonNull List<Day> mDays, @NonNull OnItemClickListener mOnItemClickListener) {
        this.mDays = mDays;
        this.mOnItemClickListener = mOnItemClickListener;
        this.mDateStartPos = (mDays.get(0).getDayOfWeek() + 6 - Setting.date_offset) % 7 + 7;
        this.mEndPosition = mDateStartPos + mDays.size();
    }

    public void updateStartDate() {
        this.mDateStartPos = (mDays.get(0).getDayOfWeek() + 6 - Setting.date_offset) % 7 + 7;
        this.mEndPosition = mDateStartPos + mDays.size();
    }

    public void setSelectedDay(int day) {
        this.mSelected = this.mDateStartPos + day - 1;
        notifyItemChanged(mSelected);
        mOnItemClickListener.onDayClick(mSelected, mDays.get(mSelected - mDateStartPos));
    }

    @Override
    public int getItemViewType(int position) {
        if (isToday(position)) {
            return VIEW_TODAY;
        }
        return position < 7 ? VIEW_WEEK : (mSelected == position ? VIEW_SELECTED : VIEW_DAY);
    }

    private boolean isToday(int position) {
        if (isPositionInMonth(position)) {
            Day day = mDays.get(position - mDateStartPos);
            if (day.isToday()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View contentView;
        switch (viewType) {
            case VIEW_DAY:
            case VIEW_SELECTED:
                contentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
                holder = new DayViewHolder(contentView);
                break;
            case VIEW_TODAY:
                contentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_today, parent, false);
                holder = new DayViewHolder(contentView);
                break;
            case VIEW_WEEK:
                contentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week, parent, false);
                holder = new RecyclerView.ViewHolder(contentView) {
                };
                break;

        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case VIEW_WEEK:
                TextView tv = (TextView) holder.itemView;
                int index = (position + Setting.date_offset) % WEEK_ARRAY.length;
                tv.setText(WEEK_ARRAY[index]);
                break;
            case VIEW_DAY:
            case VIEW_SELECTED:
            case VIEW_TODAY:
                if (isPositionInMonth(position)) {
                    setupViewOfDay((DayViewHolder) holder, position);
                }
                break;
        }
    }

    private void setupViewOfDay(DayViewHolder holder, final int position) {
        DayViewHolder dayViewHolder = holder;
        Day day = mDays.get(position - mDateStartPos);
        dayViewHolder.event.setVisibility(getEventVisibility(day));
        dayViewHolder.selected.setVisibility(position == mSelected ? View.VISIBLE : View.INVISIBLE);

        dayViewHolder.tvGreg.setText(String.valueOf(day.getDayOfMonth()));
        dayViewHolder.tvLunar.setText(day.hasBirthday() ? "生日" : day.getLunar());
        dayViewHolder.birth.setVisibility(day.hasBirthday() ? View.VISIBLE : View.INVISIBLE);

        setTextColor(position, dayViewHolder, day);
        dayViewHolder.viewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position >= 7) {
                    mSelected = position;
                    notifyItemChanged(mSelected);
                    mOnItemClickListener.onDayClick(position, mDays.get(position - mDateStartPos));
                }
            }
        });
    }

    private void setTextColor(int position, DayViewHolder dayViewHolder, Day day) {
        if (isWeekend(day.getDayOfWeek())) {
            dayViewHolder.tvGreg.setTextColor(Color.parseColor("#FC9883"));
            dayViewHolder.tvLunar.setTextColor(Color.parseColor("#FC9883"));
        } else if (isToday(position)) {
            dayViewHolder.tvGreg.setTextColor(Color.parseColor("#FFFFFF"));
            dayViewHolder.tvLunar.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            dayViewHolder.tvGreg.setTextColor(Color.parseColor("#dd000000"));
            dayViewHolder.tvLunar.setTextColor(Color.parseColor("#dd000000"));
        }
    }

    private boolean isWeekend(int dayOfWeek) {
        return dayOfWeek == 7 || dayOfWeek == 1;
    }

    private boolean isPositionInMonth(int position) {
        return position >= mDateStartPos && position < mEndPosition;
    }

    private int getEventVisibility(Day day) {
        return day.hasEvent() ? View.VISIBLE : View.INVISIBLE;
    }

    @Override
    public int getItemCount() {
        return 49;
    }

    public interface OnItemClickListener {
        void onDayClick(int position, Day day);
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {

        TextView tvGreg;
        TextView tvLunar;
        ViewGroup viewGroup;
        View selected;
        View event;
        View birth;

        public DayViewHolder(View itemView) {
            super(itemView);
            tvGreg = (TextView) itemView.findViewById(R.id.tv_greg);
            tvLunar = (TextView) itemView.findViewById(R.id.tv_lunar);
            viewGroup = (ViewGroup) itemView.findViewById(R.id.rl);
            selected = itemView.findViewById(R.id.fl_select);
            event = itemView.findViewById(R.id.fl_event);
            birth = itemView.findViewById(R.id.iv_birth);
        }
    }
}
