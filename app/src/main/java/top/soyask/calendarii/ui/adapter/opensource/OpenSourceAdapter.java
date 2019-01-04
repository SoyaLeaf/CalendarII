package top.soyask.calendarii.ui.adapter.opensource;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.OpenSource;

/**
 * Created by mxf on 2017/10/29.
 */

public class OpenSourceAdapter extends RecyclerView.Adapter<OpenSourceAdapter.OpenSourceViewHolder> {

    private List<OpenSource> mOpenSources;
    private OnOpenSourceClickListener mOnOpenSourceClickListener;

    public OpenSourceAdapter(List<OpenSource> openSources, OnOpenSourceClickListener onOpenSourceClickListener) {
        this.mOpenSources = openSources;
        this.mOnOpenSourceClickListener = onOpenSourceClickListener;
    }

    @Override
    public OpenSourceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_os, parent, false);
        return new OpenSourceViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(OpenSourceViewHolder holder, int position) {
        final OpenSource openSource = mOpenSources.get(position);
        holder.tv_title.setText(openSource.getTitle());
        holder.tv_content.setText(openSource.getDetail());
        holder.itemView.setOnClickListener(v -> mOnOpenSourceClickListener.onOpenSourceClick(openSource.getUrl()));
    }

    @Override
    public int getItemCount() {
        return mOpenSources.size();
    }

    public interface OnOpenSourceClickListener {
        void onOpenSourceClick(String url);
    }

    public static class OpenSourceViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_content;

        public OpenSourceViewHolder(View itemView) {
            super(itemView);
            tv_title =  itemView.findViewById(R.id.tv_title);
            tv_content =  itemView.findViewById(R.id.tv_content);
        }
    }
}
