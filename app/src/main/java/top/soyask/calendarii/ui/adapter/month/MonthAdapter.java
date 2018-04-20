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
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;

/**
 * Created by mxf on 2017/8/8.
 */
public class MonthAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static final int VIEW_WEEK = 0;
    static final int VIEW_DAY = 1;
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
                if (Setting.day_week_text_size != -1) {
                    tv.setTextSize(Setting.day_week_text_size);
                }else {
                    tv.setTextSize(Global.DEFAULT_WEEK_SIZE);
                }
                break;
            case VIEW_DAY:
            case VIEW_SELECTED:
            case VIEW_TODAY:
                setupViewSize((DayViewHolder) holder);
                if (isPositionInMonth(position)) {
                    setupViewOfDay((DayViewHolder) holder, position);
                } else {
                    clearViewOfDay((DayViewHolder) holder);
                }
                break;
        }
    }

    private void clearViewOfDay(DayViewHolder holder) {
        holder.event.setVisibility(View.INVISIBLE);
        holder.selected.setVisibility(View.INVISIBLE);
        holder.birth.setVisibility(View.INVISIBLE);
        holder.tv_holiday.setVisibility(View.INVISIBLE);
        holder.tv_work.setVisibility(View.INVISIBLE);
        holder.tv_greg.setText(null);
        holder.tv_lunar.setText(null);
    }

    private void setupViewOfDay(DayViewHolder holder, final int position) {
        Day day = mDays.get(position - mDateStartPos);
        holder.event.setVisibility(getEventVisibility(day));
        holder.selected.setVisibility(position == mSelected ? View.VISIBLE : View.INVISIBLE);
        holder.birth.setVisibility(day.hasBirthday() ? View.VISIBLE : View.INVISIBLE);
        holder.tv_holiday.setVisibility(day.isHoliday() ? View.VISIBLE : View.INVISIBLE);
        holder.tv_work.setVisibility(day.isWorkday() ? View.VISIBLE : View.INVISIBLE);
        holder.tv_greg.setText(String.valueOf(day.getDayOfMonth()));
        holder.tv_lunar.setText(day.hasBirthday() ?
                holder.itemView.getResources().getString(R.string.birthday) : day.getLunar().getSimpleLunar());
        setTextColor(position, holder, day);
        holder.view_group.setOnClickListener(v -> {
            if (position >= 7) {
                mSelected = position;
                notifyItemChanged(mSelected);
                mOnItemClickListener.onDayClick(position, mDays.get(position - mDateStartPos));
            }
        });
    }

    private void setupViewSize(DayViewHolder holder) {
        if (Setting.day_number_text_size != -1) {
            holder.tv_greg.setTextSize(Setting.day_number_text_size);
        }else {
            holder.tv_greg.setTextSize(Global.DEFAULT_NUMBER_SIZE);
        }

        if (Setting.day_lunar_text_size != -1) {
            holder.tv_lunar.setTextSize(Setting.day_lunar_text_size);
        }else {
            holder.tv_lunar.setTextSize(Global.DEFAULT_LUNAR_SIZE);
        }

        if (Setting.day_holiday_text_size != -1) {
            holder.tv_holiday.setTextSize(Setting.day_holiday_text_size);
            holder.tv_work.setTextSize(Setting.day_holiday_text_size);
        }else {
            holder.tv_holiday.setTextSize(Global.DEFAULT_HOLIDAY_SIZE);
            holder.tv_work.setTextSize(Global.DEFAULT_HOLIDAY_SIZE);
        }

        if (Setting.day_size != -1) {
            holder.view_group.getLayoutParams().width = Setting.day_size;
            holder.view_group.getLayoutParams().height = Setting.day_size;
        }else {
            int size = holder.itemView.getResources().getDimensionPixelSize(R.dimen.item_day_size);
            holder.view_group.getLayoutParams().width =  size;
            holder.view_group.getLayoutParams().height =  size;
        }
        holder.itemView.postInvalidate();
    }

    private void setTextColor(int position, DayViewHolder dayViewHolder, Day day) {
        if (isToday(position)) {
            dayViewHolder.tv_greg.setTextColor(Color.parseColor("#FFFFFF"));
            dayViewHolder.tv_lunar.setTextColor(Color.parseColor("#FFFFFF"));
        } else if (isWeekend(day.getDayOfWeek())) {
            dayViewHolder.tv_greg.setTextColor(Color.parseColor("#FC9883"));
            dayViewHolder.tv_lunar.setTextColor(Color.parseColor("#FC9883"));
        } else {
            dayViewHolder.tv_greg.setTextColor(Color.parseColor("#dd000000"));
            dayViewHolder.tv_lunar.setTextColor(Color.parseColor("#dd000000"));
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

        TextView tv_greg;
        TextView tv_lunar;
        ViewGroup view_group;
        View selected;
        View event;
        View birth;
        TextView tv_holiday;
        TextView tv_work;

        public DayViewHolder(View itemView) {
            super(itemView);
            tv_greg = itemView.findViewById(R.id.tv_greg);
            tv_lunar = itemView.findViewById(R.id.tv_lunar);
            view_group = itemView.findViewById(R.id.rl);
            selected = itemView.findViewById(R.id.fl_select);
            event = itemView.findViewById(R.id.fl_event);
            birth = itemView.findViewById(R.id.iv_birth);
            tv_holiday = itemView.findViewById(R.id.tv_holiday);
            tv_work = itemView.findViewById(R.id.tv_work);
        }
    }
}
