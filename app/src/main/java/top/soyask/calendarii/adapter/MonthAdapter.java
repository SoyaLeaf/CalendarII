package top.soyask.calendarii.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.domain.Day;

/**
 * Created by mxf on 2017/8/8.
 */
public class MonthAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static final int VIEW_WEEK = 0; //显示星期
    static final int VIEW_DAY = 1; //显示日子
    static final int VIEW_SELECTED = 3;
    static final int VIEW_TODAY = 4;
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
        this.mDateStartPos = mDays.get(0).getDayOfWeek() + 6;
        this.mEndPosition = mDateStartPos + mDays.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (position > mDateStartPos && position < mEndPosition) {
            Day day = mDays.get(position - mDateStartPos);
            if (day.isToday()) {
                return VIEW_TODAY;
            }
//            if (!day.getEvents().isEmpty()) {
//                return VIEW_EVENT;
//            }
        }
        int type = position < 7 ? VIEW_WEEK : (position == mSelected ? VIEW_SELECTED : VIEW_DAY);
        return type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View contentView;
        switch (viewType) {
            case VIEW_DAY:
                contentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
                holder = new DayViewHolder(contentView);
                break;
            case VIEW_EVENT:
                contentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_event, parent, false);
                holder = new DayViewHolder(contentView);
                break;
            case VIEW_SELECTED:
                contentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_select, parent, false);
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
                tv.setText(WEEK_ARRAY[position]);
                break;
            case VIEW_DAY:
            case VIEW_SELECTED:
            case VIEW_TODAY:
            case VIEW_EVENT:
                if (position >= mDateStartPos && position < mEndPosition ) {

                    DayViewHolder dayViewHolder = (DayViewHolder) holder;
                    Day day = mDays.get(position - mDateStartPos);
                    dayViewHolder.tvGreg.setText(String.valueOf(day.getDayOfMonth()));
                    dayViewHolder.tvLunar.setText(day.getLunar());
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
                break;

        }
    }

    @Override
    public int getItemCount() {
        return 49;
//        return mDays.size() + mDateStartPos;
    }

    public interface OnItemClickListener {
        void onDayClick(int position, Day day);
    }

    public static class DayViewHolder extends RecyclerView.ViewHolder {

        TextView tvGreg;
        TextView tvLunar;
        ViewGroup viewGroup;

        public DayViewHolder(View itemView) {
            super(itemView);
            tvGreg = (TextView) itemView.findViewById(R.id.tv_greg);
            tvLunar = (TextView) itemView.findViewById(R.id.tv_lunar);
            viewGroup = (ViewGroup) itemView.findViewById(R.id.rl);
        }
    }
}
