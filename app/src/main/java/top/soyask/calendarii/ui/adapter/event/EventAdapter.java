package top.soyask.calendarii.ui.adapter.event;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.ui.adapter.viewholder.EventViewHolder;
import top.soyask.calendarii.entity.Event;

/**
 * Created by mxf on 2017/8/11.
 */
public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Event> mEvents;
    private OnEventItemClickListener mOnEventItemClickListener;

    public EventAdapter(List<Event> events, OnEventItemClickListener onEventItemClickListener) {
        this.mEvents = events;
        this.mOnEventItemClickListener = onEventItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final EventViewHolder eventViewHolder = (EventViewHolder) holder;
        Event event = mEvents.get(position);
        eventViewHolder.tv_title.getPaint().setAntiAlias(true);
        eventViewHolder.tv_event.getPaint().setAntiAlias(true);
        eventViewHolder.tv_title.getPaint().setFlags(event.isComplete() ? Paint.STRIKE_THRU_TEXT_FLAG : 0);
        eventViewHolder.tv_event.getPaint().setFlags(event.isComplete() ? Paint.STRIKE_THRU_TEXT_FLAG : 0);
        eventViewHolder.tv_title.setText(event.getTitle());
        eventViewHolder.tv_event.setText(event.getDetail());
        eventViewHolder.collapse();
        eventViewHolder.ib_edit.setOnClickListener(v -> {
            if (mOnEventItemClickListener != null) {
                mOnEventItemClickListener.onEditClick(position, mEvents.get(position));
            }
        });

        eventViewHolder.ib_delete.setOnClickListener(v -> {
            if (mOnEventItemClickListener != null) {
                eventViewHolder.collapse();
                mOnEventItemClickListener.onDeleteClick(position, mEvents.get(position));
            }
        });

        eventViewHolder.ib_share.setOnClickListener(v -> mOnEventItemClickListener.onShare(mEvents.get(position)));

        eventViewHolder.setOnTextPressListener(new EventViewHolder.OnTextPressListener() {
            @Override
            public void onCross() {
                if (mOnEventItemClickListener != null) {
                    mOnEventItemClickListener.onComplete(position, mEvents.get(position));
                }
            }

            @Override
            public void onLineClear() {
                if (mOnEventItemClickListener != null) {
                    mOnEventItemClickListener.onCompleteCancel(position, mEvents.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public interface OnEventItemClickListener {
        void onEditClick(int position, Event event);

        void onDeleteClick(int position, Event event);

        void onComplete(int position, Event event);

        void onCompleteCancel(int position, Event event);

        void onShare(Event event);
    }


}
