package top.soyask.calendarii.ui.fragment.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import top.soyask.calendarii.R;

public class EditBottomDialogFragment extends BottomSheetDialogFragment {

    private static final String DEFAULT_TEXT = "default_text";
    private static final String HINT = "hint";
    private EditText mEditText;
    private OnDoneListener mOnDoneListener;
    private OnDismissListener mOnDismissListener;

    public static EditBottomDialogFragment newInstance(String defaultText, String hint) {

        Bundle args = new Bundle();

        EditBottomDialogFragment fragment = new EditBottomDialogFragment();
        args.putString(DEFAULT_TEXT, defaultText);
        args.putString(HINT, hint);
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
            String hint = arguments.getString(HINT);
            String defaultText = arguments.getString(DEFAULT_TEXT);
            mEditText.setHint(hint);
            if (!defaultText.isEmpty()) {
                mEditText.setText(defaultText);
                mEditText.setSelection(defaultText.length());
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
        if (mOnDoneListener != null) {
            mOnDoneListener.onDone(comment);
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
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss();
        }
    }

    public interface OnDoneListener {
        void onDone(String result);
    }

    public interface OnDismissListener {
        void onDismiss();
    }
}
