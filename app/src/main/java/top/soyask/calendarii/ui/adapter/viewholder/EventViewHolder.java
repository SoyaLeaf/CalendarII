package top.soyask.calendarii.ui.adapter.viewholder;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.cachapa.expandablelayout.ExpandableLayout;

import top.soyask.calendarii.R;

/**
 * Created by mxf on 2017/8/16.
 */
public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {

    public ExpandableLayout el;
    public ImageButton ib_down;
    public ImageButton ib_up;
    public ImageButton ib_delete;
    public ImageButton ib_edit;
    public ImageButton ib_share;
    public TextView tv_title;
    public TextView tv_event;
    private OnTextPressListener mOnTextPressListener;

    public EventViewHolder(View itemView) {
        super(itemView);
        findView(itemView);
        init();
    }

    private void findView(View itemView) {
        el = itemView.findViewById(R.id.el);
        ib_down = itemView.findViewById(R.id.ib_down);
        ib_up = itemView.findViewById(R.id.ib_up);
        ib_delete = itemView.findViewById(R.id.ib_delete);
        ib_edit = itemView.findViewById(R.id.ib_edit);
        ib_share = itemView.findViewById(R.id.ib_share);
        tv_title = itemView.findViewById(R.id.tv_title);
        tv_event = itemView.findViewById(R.id.tv_event);
    }

    private void init() {
        itemView.setOnTouchListener(this);
        ib_down.setOnClickListener(this);
        ib_up.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_up:
                collapse();
                break;
            case R.id.ib_down:
                expand();
                break;
        }
    }

    public void expand() {
        el.expand();
        ib_down.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float elevation = itemView.getResources().getDimension(R.dimen.card_float_elevation);
            itemView.setElevation(elevation);
        }
    }

    public void collapse() {
        el.collapse();
        ib_down.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            itemView.setElevation(0);
        }
    }

    public void setOnTextPressListener(OnTextPressListener onTextPressListener) {
        this.mOnTextPressListener = onTextPressListener;
    }

    float startX;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getRawX();
                itemView.setAlpha(0.8f);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                onTouchCancel(event);
                break;
        }
        return true;
    }


    private void onTouchCancel(MotionEvent event) {
        itemView.setAlpha(1f);
        float rawX = event.getRawX();
        int width = tv_title.getWidth() / 4;
        if (rawX - startX > width) {
            if (mOnTextPressListener != null) {
                mOnTextPressListener.onCross();
            }
        } else if (startX - rawX > width) {
            if (mOnTextPressListener != null) {
                mOnTextPressListener.onLineClear();
            }
        }
    }

    public interface OnTextPressListener {
        void onCross();

        void onLineClear();
    }
}