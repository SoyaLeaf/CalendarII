package top.soyask.calendarii.ui.fragment.setting.symbol;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Symbol;
import top.soyask.calendarii.global.Global;
import top.soyask.calendarii.global.Setting;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;

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
            String comment = Setting.symbol_comment.get(symbol.KEY);
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
                SymbolDialogFragment fragment = SymbolDialogFragment.newInstance(position);
                fragment.setOnDismissListener(SymbolFragment.this::dialogDismiss);
                fragment.setOnDoneListener(SymbolFragment.this::update);
                fragment.show(getFragmentManager(), null);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return Symbol.values().length;
        }
    };

    private void dialogDismiss() {
        mContentView.requestFocus();
        mContentView.postDelayed(this::hideSoftInput, 200);
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

    private void update() {
        mAdapter.notifyDataSetChanged();
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
