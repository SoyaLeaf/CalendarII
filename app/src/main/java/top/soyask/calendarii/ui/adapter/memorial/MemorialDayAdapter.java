package top.soyask.calendarii.ui.adapter.memorial;

import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.MemorialDay;
import top.soyask.calendarii.global.Global;

public class MemorialDayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MemorialDay> mMemorialDays;
    private MemorialDayActionListener mMemorialDayActionListener;

    public MemorialDayAdapter(List<MemorialDay> memorialDays, MemorialDayActionListener listener) {
        this.mMemorialDays = memorialDays;
        this.mMemorialDayActionListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_item_memorial, parent, false);
        return new MemorialDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MemorialDay memorialDay = mMemorialDays.get(position);
        Resources resources = holder.itemView.getResources();
        String title = getTitle(memorialDay, resources);
        String details = memorialDay.getDetails();
        String date = resources
                .getString(R.string.date_format, memorialDay.getMonth(), memorialDay.getDay());
        String lunar = memorialDay.getLunar();
        date = getDateText(memorialDay, resources, date, lunar);
        MemorialDayViewHolder viewHolder = (MemorialDayViewHolder) holder;
        viewHolder.tvTitle.setText(title);
        viewHolder.tvDate.setText(date);
        if (details == null || details.isEmpty()) {
            viewHolder.tvDetails.setVisibility(View.GONE);
        } else {
            viewHolder.tvDetails.setText(details);
            viewHolder.tvDetails.setVisibility(View.VISIBLE);
        }

        viewHolder.itemView.setOnClickListener(
                v -> mMemorialDayActionListener.onMemorialDayClick(position, memorialDay));
        viewHolder.itemView.setOnLongClickListener(
                v -> mMemorialDayActionListener.onMemorialDayLongClick(position, memorialDay));
    }

    private String getDateText(MemorialDay memorialDay, Resources resources, String date, String lunar) {
        if (memorialDay.isLunar()) {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(Calendar.YEAR, memorialDay.getYear());
            calendar.set(Calendar.MONTH, memorialDay.getMonth() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, memorialDay.getDay());
            long days = calendar.getTimeInMillis() / DateUtils.DAY_IN_MILLIS;
            long currentDays = System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS;
            int delta = Long.valueOf(currentDays - days).intValue();
            date += " " + lunar;
            if (delta > 0) {
                date += " " + resources.getString(R.string.it_has_been_xx_days, delta);
            }
        } else {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            int year = calendar.get(Calendar.YEAR);
            calendar.setTimeInMillis(0);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, memorialDay.getMonth() - 1);
            calendar.set(Calendar.DAY_OF_MONTH, memorialDay.getDay());
            long days = calendar.getTimeInMillis() / DateUtils.DAY_IN_MILLIS;
            long currentDays = System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS;
            int delta = Long.valueOf(currentDays - days).intValue();
            int deltaYear = year - memorialDay.getYear();
            if (deltaYear > 0) {
                if (delta == 0) {
                    date += " " + resources.getString(R.string.xx_anniversary, deltaYear);
                } else {
                    date += " " + resources.getString(R.string.offset_of_year, deltaYear);
                }
            }
            if (delta < 0) {
                date += " " + resources.getString(R.string.xx_day_ago, -delta);
            } else if (delta > 0) {
                date += " " + resources.getString(R.string.has_been_xx_days, delta);
            }
        }
        return date;
    }

    private String getTitle(MemorialDay memorialDay, Resources resources) {
        String who = memorialDay.getWho();
        String[] whos = who.split(Global.FLAG);
        StringBuilder title = new StringBuilder();
        for (int i = 0; i < whos.length; i++) {
            if (i > 0 && i == whos.length - 1) {
                title.append(resources.getString(R.string.and));
            } else if (i > 0) {
                title.append(resources.getString(R.string.name_separator));
            }
            title.append(whos[i]);
        }
        String name = memorialDay.getName();
        return title.append(resources.getString(R.string.of)).append(name).toString();
    }

    @Override
    public int getItemCount() {
        return mMemorialDays.size();
    }

    public interface MemorialDayActionListener {
        void onMemorialDayClick(int position, MemorialDay day);

        boolean onMemorialDayLongClick(int position, MemorialDay day);
    }
}
