package top.soyask.calendarii.ui.adapter.birth;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Birthday;

public class BirthdayAdapter extends RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder> {

    private List<Birthday> mBirthdays;
    private OnBirthdayClickListener mOnBirthdayClickListener;

    public BirthdayAdapter(List<Birthday> birthdays, OnBirthdayClickListener onBirthdayClickListener) {
        this.mBirthdays = birthdays;
        this.mOnBirthdayClickListener = onBirthdayClickListener;
    }

    @Override
    public BirthdayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_birth, parent, false);
        return new BirthdayViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(BirthdayViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(null);
        holder.itemView.setOnLongClickListener(null);
        holder.itemView.setAlpha(1f);
        if (position == mBirthdays.size()) {
            holder.tv_who.setText("新增生日");
            holder.tv_when.setText("+");
            holder.itemView.setOnClickListener(v -> mOnBirthdayClickListener.addBirthday());
            holder.itemView.setAlpha(0.2f);
        } else {
            final Birthday birthday = mBirthdays.get(position);
            holder.tv_when.setText(birthday.getWhen());
            holder.tv_who.setText(birthday.getWho() + "的生日");
            holder.itemView.setOnLongClickListener(v -> {
                mOnBirthdayClickListener.onBirthdayLongClick(position, birthday);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return mBirthdays.size() + 1;
    }

    public interface OnBirthdayClickListener {
        void onBirthdayClick();

        void onBirthdayLongClick(int position, Birthday birthday);

        void addBirthday();
    }

    public static class BirthdayViewHolder extends RecyclerView.ViewHolder {

        TextView tv_who;
        TextView tv_when;

        public BirthdayViewHolder(View itemView) {
            super(itemView);
            tv_who =  itemView.findViewById(R.id.tv_who);
            tv_when =  itemView.findViewById(R.id.tv_when);
        }
    }
}
