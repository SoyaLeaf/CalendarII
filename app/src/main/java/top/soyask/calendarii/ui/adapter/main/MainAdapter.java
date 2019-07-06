package top.soyask.calendarii.ui.adapter.main;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.MemorialDay;
import top.soyask.calendarii.entity.Thing;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.ui.view.PacManView;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_MEMORIAL_DAY = 0;
    private static final int VIEW_THING = 1;
    private static final int VIEW_BOTTOM_EMPTY = -1;
    private List<Thing> mThings = Collections.emptyList();
    private List<MemorialDay> mMemorialDays = Collections.emptyList();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_MEMORIAL_DAY:
                return new MemorialDayViewHolder(
                        inflater.inflate(R.layout.recycler_item_main_mamorial, parent, false));
            case VIEW_THING:
                return new ThingViewHolder(
                        inflater.inflate(R.layout.recycler_item_main_thing, parent, false));
            case VIEW_BOTTOM_EMPTY:
            default:
                return new RecyclerView.ViewHolder(
                        inflater.inflate(R.layout.recycler_item_main_bottom_empty, parent, false)) {
                };
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= mMemorialDays.size() + mThings.size()) {
            return VIEW_BOTTOM_EMPTY;
        }
        if (position >= mMemorialDays.size()) {
            return VIEW_THING;
        }
        return VIEW_MEMORIAL_DAY;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Resources resources = holder.itemView.getResources();
        int viewType = getItemViewType(position);
        switch (viewType) {
            case VIEW_MEMORIAL_DAY:
                onBindMemorialDay((MemorialDayViewHolder) holder, resources, position);
                break;
            case VIEW_THING:
                onBindThing((ThingViewHolder) holder, position - mMemorialDays.size());
                break;
            case VIEW_BOTTOM_EMPTY:
                holder.itemView.setOnClickListener(v -> {
                    PacManView pacManView = holder.itemView.findViewById(R.id.pmv);
                    pacManView.setCallback(() -> {

                    });
                    pacManView.start();
                });
                break;
        }
    }

    private void onBindMemorialDay(MemorialDayViewHolder holder, Resources resources, int position) {
        holder.divider.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        MemorialDay memorialDay = mMemorialDays.get(position);
        String title = getTitle(memorialDay, resources);
        holder.tv_name.setText(title);
        String details = memorialDay.getDetails();
        if (TextUtils.isEmpty(details)) {
            holder.tv_content.setVisibility(View.GONE);
        } else {
            holder.tv_content.setText(details);
            holder.tv_content.setVisibility(View.VISIBLE);
        }
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        int yearCount = calendar.get(Calendar.YEAR) - memorialDay.getYear();
        if (yearCount <= 0) {
            holder.tv_year_count.setVisibility(View.GONE);
        } else {
            holder.tv_year_count.setVisibility(View.VISIBLE);
            holder.tv_year_count.setText(resources.getString(R.string.xx_anniversary, yearCount));
        }
    }

    private void onBindThing(ThingViewHolder holder, int position) {
        holder.divider.setVisibility(mMemorialDays.isEmpty() && position == 0 ? View.INVISIBLE : View.VISIBLE);
        Thing thing = mThings.get(position);
        holder.tv.setText(thing.getDetail());
    }

    public void setMemorialDays(List<MemorialDay> memorialDays) {
        this.mMemorialDays = memorialDays;
    }

    public void setThings(List<Thing> things) {
        this.mThings = things;
    }

    @Override
    public int getItemCount() {
        int count = mThings.size() + mMemorialDays.size();
        return count < 2 ? count + 1 : count;
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
}
