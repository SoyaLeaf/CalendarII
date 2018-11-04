package top.soyask.calendarii.ui.fragment.setting.symbol;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Symbol;
import top.soyask.calendarii.global.Setting;

public class SymbolDialogFragment extends BottomSheetDialogFragment {

    public static final String POSITION = "position";
    private EditText mEditText;
    private Symbol mSymbol;
    private OnDoneListener mOnDoneListener;
    private OnDismissListener mOnDismissListener;

    public static SymbolDialogFragment newInstance(int position) {

        Bundle args = new Bundle();

        SymbolDialogFragment fragment = new SymbolDialogFragment();
        args.putInt(POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_symbol_comment, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditText = view.findViewById(R.id.et);
        Bundle arguments = getArguments();
        if (arguments != null) {
            int position = arguments.getInt(POSITION);
            mSymbol = Symbol.values()[position];
            String comment = Setting.symbol_comment.get(mSymbol.KEY);
            if (!"默认".equals(comment)) {
                mEditText.setText(comment);
                mEditText.setSelection(comment.length());
            }
        }
        mEditText.requestFocus();
        view.findViewById(R.id.btn).setOnClickListener(this::done);
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(this::showSoftInput, 200);
    }

    private void showSoftInput() {
        try {
            InputMethodManager methodManager = (InputMethodManager)
                    Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
            assert methodManager != null;
            methodManager.showSoftInput(mEditText, InputMethodManager.SHOW_FORCED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void done(View view) {
        String comment = mEditText.getText().toString();
        if (comment.isEmpty()) {
            comment = "默认";
            Setting.remove(getContext(), mSymbol.KEY);
        } else {
            Setting.setting(getContext(), mSymbol.KEY, comment);
        }
        Setting.symbol_comment.put(mSymbol.KEY, comment);
        if (mOnDoneListener != null) {
            mOnDoneListener.onDone();
        }
        dismiss();
    }

    public void setOnDoneListener(OnDoneListener listener) {
        this.mOnDoneListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss();
        }
    }

    public interface OnDoneListener {
        void onDone();
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}
