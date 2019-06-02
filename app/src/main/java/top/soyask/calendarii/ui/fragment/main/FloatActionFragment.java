package top.soyask.calendarii.ui.fragment.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import top.soyask.calendarii.R;
import top.soyask.calendarii.entity.Day;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;

public class FloatActionFragment extends BaseFragment {

    private static final String DAY = "Day";
    private boolean exit = false;
    private ActionClickCallback mCallback;

    public FloatActionFragment() {
        super(R.layout.fragment_float_action);
    }


    public static FloatActionFragment newInstance(Day day) {
        Bundle args = new Bundle();
        FloatActionFragment fragment = new FloatActionFragment();
        args.putSerializable(DAY, day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        View view = findViewById(R.id.fab_close);
        findViewById(R.id.fab_thing).setOnClickListener(v -> {
            removeFragment(FloatActionFragment.this);
            mCallback.onAddThingClick();
        });

        findViewById(R.id.fab_memorial).setOnClickListener(v -> {
            removeFragment(FloatActionFragment.this);
            mCallback.onAddMemorialClick();
        });
        ObjectAnimator
                .ofFloat(view, "rotation", 0, 45)
                .setDuration(100)
                .start();

        view.setOnClickListener(this::exit);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setOnTouchListener((v, event) -> {
            new Handler().post(() -> exit(findViewById(R.id.fab_close)));
            return true;
        });
    }

    public void setCallback(ActionClickCallback callback) {
        this.mCallback = callback;
    }

    private void exit(View view) {
        if (exit) {
            return;
        }
        exit = true;
        ObjectAnimator animator = ObjectAnimator
                .ofFloat(view, "rotation", 45, 0)
                .setDuration(100);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                removeFragment(FloatActionFragment.this);
            }
        });
        animator.start();
    }

    public interface ActionClickCallback {
        void onAddThingClick();
        void onAddMemorialClick();
    }
}
