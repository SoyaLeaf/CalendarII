package top.soyask.calendarii.fragment.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import top.soyask.calendarii.MainActivity;
import top.soyask.calendarii.R;

/**
 * Created by mxf on 2017/6/23.
 */
public abstract class BaseFragment extends Fragment {

    private View mContentView;
    private int mLayout;
    private MainActivity mMainActivity;

    protected BaseFragment(@LayoutRes int layout) {
        mLayout = layout;
    }

    protected <T extends View> T findViewById(@IdRes int id) {
        return (T) mContentView.findViewById(id);
    }
    protected Toolbar findToolbar(@IdRes int id){
        return findViewById(id);
    }

    protected Toolbar findToolbar(){
        return findToolbar(R.id.toolbar);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mMainActivity = (MainActivity) context;
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
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }


    /**
     * 添加新的Fragment到页面最上层
     *
     * @param fragment
     */
    protected void addFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_bottom, R.anim.in_from_bottom, R.anim.out_to_bottom)
                .add(R.id.main, fragment)
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
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mContentView, tip, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    protected void showSnackbar(final String tip, final String action, final View.OnClickListener listener) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(mContentView, tip, Snackbar.LENGTH_SHORT).setAction(action,listener).show();
            }
        });
    }

    /**
     * 对Fragment的UI进行设置
     */
    protected abstract void setupUI();

    public View getContentView() {
        return mContentView;
    }

    public MainActivity getMainActivity() {
        return mMainActivity;
    }

}
