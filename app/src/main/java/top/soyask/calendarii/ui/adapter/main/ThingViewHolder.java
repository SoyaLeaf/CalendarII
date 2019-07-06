package top.soyask.calendarii.ui.adapter.main;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import top.soyask.calendarii.R;

class ThingViewHolder extends RecyclerView.ViewHolder {

    ImageView iv;
    TextView tv;
    View divider;

    public ThingViewHolder(@NonNull View itemView) {
        super(itemView);
        iv = itemView.findViewById(R.id.iv);
        tv = itemView.findViewById(R.id.tv);
        divider = itemView.findViewById(R.id.divider);
    }
}
