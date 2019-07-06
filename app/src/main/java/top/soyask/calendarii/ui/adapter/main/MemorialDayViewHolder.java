package top.soyask.calendarii.ui.adapter.main;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import top.soyask.calendarii.R;

class MemorialDayViewHolder extends RecyclerView.ViewHolder {

    TextView tv_name;
    TextView tv_content;
    TextView tv_year_count;

    MemorialDayViewHolder(@NonNull View itemView) {
        super(itemView);
        tv_name = itemView.findViewById(R.id.tv_name);
        tv_content = itemView.findViewById(R.id.tv_content);
        tv_year_count = itemView.findViewById(R.id.tv_year_count);
    }
}
