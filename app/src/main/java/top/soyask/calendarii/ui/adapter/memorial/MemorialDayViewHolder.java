package top.soyask.calendarii.ui.adapter.memorial;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import top.soyask.calendarii.R;

class MemorialDayViewHolder extends RecyclerView.ViewHolder {

    TextView tvTitle;
    TextView tvDetails;
    TextView tvDate;

    MemorialDayViewHolder(@NonNull View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tv_title);
        tvDetails = itemView.findViewById(R.id.tv_details);
        tvDate = itemView.findViewById(R.id.tv_date);
    }
}
