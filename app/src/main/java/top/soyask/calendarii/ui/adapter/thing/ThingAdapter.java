package top.soyask.calendarii.ui.adapter.thing;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.RecyclerView;
import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Thing;

public class ThingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final SimpleDateFormat sDateFormat =
            new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);

    private List<Thing> mThings;
    private ThingActionCallback mCallback;

    public ThingAdapter(List<Thing> things,ThingActionCallback callback) {
        this.mThings = things;
        this.mCallback = callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_event, parent, false);
        return new ThingViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ThingViewHolder eventViewHolder = (ThingViewHolder) holder;
        final Thing thing = mThings.get(position);
        System.out.println(new Date(thing.getTargetTime()));
        eventViewHolder.tv_title.getPaint().setAntiAlias(true);
        eventViewHolder.tv_event.getPaint().setAntiAlias(true);
        eventViewHolder.tv_title.getPaint().setFlags(thing.isDone() ? Paint.STRIKE_THRU_TEXT_FLAG : 0);
        eventViewHolder.tv_event.getPaint().setFlags(thing.isDone() ? Paint.STRIKE_THRU_TEXT_FLAG : 0);
        eventViewHolder.tv_title.setText(sDateFormat.format(new Date(thing.getTargetTime())));
        eventViewHolder.tv_event.setText(thing.getDetail());
        eventViewHolder.collapse();
        eventViewHolder.ib_edit.setOnClickListener(v -> {
            if (mCallback != null) {
                mCallback.onEditClick(position, thing);
            }
        });

        eventViewHolder.ib_delete.setOnClickListener(v -> {
            if (mCallback != null) {
                mCallback.onDeleteClick(position, thing);
            }
        });

        eventViewHolder.ib_share.setOnClickListener(v -> {
            if (mCallback != null) {
                mCallback.onShare( thing);
            }
        });

        eventViewHolder.setOnTextPressListener(new ThingViewHolder.OnTextPressListener() {
            @Override
            public void onCross() {
                if (mCallback != null) {
                    mCallback.onDone(position, thing);
                }
            }
            @Override
            public void onLineClear() {
                if (mCallback != null) {
                    mCallback.onDoneCancel(position, thing);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mThings.size();
    }

    public interface ThingActionCallback {
        void onEditClick(int position, Thing thing);

        void onDeleteClick(int position, Thing thing);

        void onDone(int position, Thing thing);

        void onDoneCancel(int position, Thing thing);

        void onShare(Thing thing);
    }
}
