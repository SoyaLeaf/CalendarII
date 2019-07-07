package top.soyask.calendarii.ui.fragment.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentTransaction;
import androidx.transition.Explode;
import top.soyask.calendarii.MainActivity;
import top.soyask.calendarii.R;

/**
 * Created by mxf on 2017/6/23.
 */
public abstract class BaseFragment extends Fragment {

    protected View mContentView;
    protected MainActivity mHostActivity;
    private int mLayout;

    protected BaseFragment(@LayoutRes int layout) {
        mLayout = layout;
    }

    protected <T extends View> T findViewById(@IdRes int id) {
        return mContentView.findViewById(id);
    }

    protected Toolbar findToolbar(@IdRes int id) {
        return findViewById(id);
    }

    protected Toolbar findToolbar() {
        return findToolbar(R.id.toolbar);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mHostActivity = (MainActivity) context;
        }
    }

    protected ActionBar setToolbar(Toolbar toolbar) {
        FragmentActivity act = getActivity();
        AppCompatActivity activity;
        if (act instanceof AppCompatActivity) {
            activity = (AppCompatActivity) act;
            activity.setSupportActionBar(toolbar);
            return activity.getSupportActionBar();
        } else {
            throw new RuntimeException("这个Activity没有继承 AppCompatActivity");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContentView = inflater.inflate(mLayout, container, false);
        setHasOptionsMenu(true);
        return mContentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postponeEnterTransition();
        view.setOnTouchListener((v, event) -> true);
    }


    /**
     * 添加新的Fragment到页面最上层
     *
     * @param fragment
     */
    protected void replaceFragment(Fragment fragment) {
        mHostActivity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.in_from_bottom,0, 0, R.anim.out_slide)
                .add(R.id.cover, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * 删除对应的Fragment
     *
     * @param fragment
     */
    protected void removeFragment(Fragment fragment) {
        getFragmentManager().popBackStack(fragment.getClass().getSimpleName(), 1);
    }

    protected void showSnackbar(final String tip) {
        mHostActivity.runOnUiThread(() -> Snackbar.make(mContentView, tip, Snackbar.LENGTH_SHORT).show());
    }

    protected void showSnackbar(final String tip, final String action, final View.OnClickListener listener) {
        mHostActivity.runOnUiThread(() -> Snackbar.make(mContentView, tip, Snackbar.LENGTH_SHORT).setAction(action, listener).show());
    }

    /**
     * 对Fragment的UI进行设置
     */
    protected abstract void setupUI();

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHostActivity = null;
    }
}
