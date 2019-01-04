package top.soyask.calendarii.ui.adapter.month;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.global.Setting;

import static top.soyask.calendarii.global.Global.VIEW_DAY;
import static top.soyask.calendarii.global.Global.VIEW_TODAY;
import static top.soyask.calendarii.global.Global.VIEW_WEEK;

public class MonthDayAdapter extends BaseAdapter {

    private List<Day> mDays;
    private int mDateStartPos;
    private int mEndPosition;
    private static final String[] WEEK_ARRAY = {"日", "一", "二", "三", "四", "五", "六",};


    public MonthDayAdapter(List<Day> days) {
        this.mDays = days;
        updateCount();
    }


    @Override
    public int getCount() {
        return 49;
    }

    @Override
    public String getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        int layout;
        switch (type) {
            case VIEW_WEEK:
                int index = (position + Setting.date_offset) % WEEK_ARRAY.length;
                layout = Setting.TransparentWidget.trans_widget_theme_color == 0 ? R.layout.item_widget_week : R.layout.item_widget_week_light;
                convertView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                ((TextView) convertView).setText(WEEK_ARRAY[index]);
                break;
            case VIEW_TODAY:
                layout = Setting.TransparentWidget.trans_widget_theme_color == 0 ? R.layout.item_widget_today : R.layout.item_widget_today_light;
                convertView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                break;
            case VIEW_DAY:
                layout = Setting.TransparentWidget.trans_widget_theme_color == 0 ? R.layout.item_widget_day : R.layout.item_widget_day_light;
                convertView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
                break;
        }
        if (position >= mDateStartPos && position < mEndPosition && position - mDateStartPos < mDays.size()) {
            Day day = mDays.get(position - mDateStartPos);
            boolean hasBirthday = day.hasBirthday();
            ((TextView) convertView.findViewById(R.id.tv_lunar)).setText(hasBirthday ? "生日" : day.getLunar().getSimpleLunar());
            ((TextView) convertView.findViewById(R.id.tv_greg)).setText(String.valueOf(day.getDayOfMonth()));
            convertView.findViewById(R.id.iv_birth).setVisibility(hasBirthday ? View.VISIBLE : View.INVISIBLE);
            convertView.findViewById(R.id.fl_event).setVisibility(day.hasEvent() ? View.VISIBLE : View.INVISIBLE);
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int type = VIEW_DAY;
        if (position >= mDateStartPos && position < mEndPosition && position - mDateStartPos < mDays.size()) {
            Day day = mDays.get(position - mDateStartPos);
            if (day.isToday()) {
                return VIEW_TODAY;
            }
        }
        type = position < 7 ? VIEW_WEEK : type;
        return type;
    }

    private void updateCount() {
        if (mDays.size() > 0) {
            this.mDateStartPos = (mDays.get(0).getDayOfWeek() + 6 - Setting.date_offset) % 7 + 7;
        } else {
            this.mDateStartPos = 6;
        }
        this.mEndPosition = mDateStartPos + mDays.size();
    }
}
