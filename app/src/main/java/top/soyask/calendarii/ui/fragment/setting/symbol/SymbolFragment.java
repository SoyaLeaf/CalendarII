package top.soyask.calendarii.ui.fragment.setting.symbol;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Objects;

import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Symbol;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;
import top.soyask.calendarii.ui.fragment.dialog.EditBottomDialogFragment;

public class SymbolFragment extends BaseFragment {

    public SymbolFragment() {
        super(R.layout.fragment_symbol);
    }

    public static SymbolFragment newInstance() {
        Bundle args = new Bundle();
        SymbolFragment fragment = new SymbolFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        findToolbar().setNavigationOnClickListener(v -> removeFragment(this));
        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(mHostActivity, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);

    }

    private RecyclerView.Adapter<SymbolViewHolder> mAdapter = new RecyclerView.Adapter<SymbolViewHolder>() {

        int[] resIds = {
                R.drawable.ic_rect_black_24dp,
                R.drawable.ic_circle_black_24dp,
                R.drawable.ic_triangle_black_24dp,
                R.drawable.ic_star_black_24dp,
                R.drawable.ic_favorite_black_24dp,
        };

        @NonNull
        @Override
        public SymbolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mHostActivity).inflate(R.layout.recycer_item_symbol, null);
            return new SymbolViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SymbolViewHolder holder, int position) {
            Symbol symbol = Symbol.values()[position];
            String comment = Setting.symbol_comment.get(symbol.key);
            holder.iv.setImageResource(resIds[position]);
            holder.tv.setText(comment);
            boolean checked = Setting.default_event_type == position;
            holder.rb.setChecked(checked);
            holder.rb.setText(checked ? "默认" : null);
            holder.itemView.setOnClickListener(v -> {
                int oldPos = Setting.default_event_type;
                Setting.default_event_type = position;
                Setting.setting(mHostActivity, Global.DEFAULT_EVENT_TYPE, Setting.default_event_type);
                notifyItemChanged(oldPos);
                notifyItemChanged(position);
            });

            holder.itemView.setOnLongClickListener(v -> {
                EditBottomDialogFragment fragment = EditBottomDialogFragment
                        .newInstance("默认".equals(comment) ? "" : comment, "自定义符号备注");
                fragment.setOnDismissListener(SymbolFragment.this::dialogDismiss);
                fragment.setOnDoneListener(result -> update(symbol, result));
                fragment.show(getFragmentManager(), null);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return Symbol.values().length;
        }
    };

    private void update(Symbol symbol, String result) {
        if (result.isEmpty()) {
            result = "默认";
            Setting.remove(getContext(), symbol.key);
        } else {
            Setting.setting(getContext(), symbol.key, result);
        }
        Setting.symbol_comment.put(symbol.key, result);
        mAdapter.notifyDataSetChanged();
    }

    private void dialogDismiss() {
        mContentView.requestFocus();
        mContentView.post(this::hideSoftInput);
    }

    private void hideSoftInput() {
        try {
            InputMethodManager methodManager = (InputMethodManager)
                    Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(methodManager).hideSoftInputFromWindow(mContentView.getApplicationWindowToken(), 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    static class SymbolViewHolder extends RecyclerView.ViewHolder {

        private final RadioButton rb;
        private final TextView tv;
        private final ImageView iv;

        SymbolViewHolder(View itemView) {
            super(itemView);
            rb = itemView.findViewById(R.id.rb);
            tv = itemView.findViewById(R.id.tv);
            iv = itemView.findViewById(R.id.iv);
        }
    }
}
