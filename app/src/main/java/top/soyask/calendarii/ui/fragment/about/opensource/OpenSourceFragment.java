package top.soyask.calendarii.ui.fragment.about.opensource;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import top.soyask.calendarii.R;
import top.soyask.calendarii.domain.OpenSource;
import top.soyask.calendarii.ui.adapter.opensource.OpenSourceAdapter;
import top.soyask.calendarii.ui.fragment.base.BaseFragment;


public class OpenSourceFragment extends BaseFragment implements OpenSourceAdapter.OnOpenSourceClickListener {

    private List<OpenSource> mOpenSources = new ArrayList<>();

    public OpenSourceFragment() {
        super(R.layout.fragment_open_source);
    }

    public static OpenSourceFragment newInstance() {
        OpenSourceFragment fragment = new OpenSourceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupUI() {
        setOpenSources();
        RecyclerView rv = findViewById(R.id.rv_os);
        OpenSourceAdapter openSourceAdapter = new OpenSourceAdapter(mOpenSources, this);
        rv.setAdapter(openSourceAdapter);
        rv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false));
        findToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFragment(OpenSourceFragment.this);
            }
        });
    }

    @Override
    public void onOpenSourceClick(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void setOpenSources(){
        OpenSource os0 = new OpenSource();
        os0.setTitle("ExpandableLayout");
        os0.setDetail("An Android layout class that supports animating the expansion and collapse of its child views.");
        os0.setUrl("https://github.com/cachapa/ExpandableLayout/blob/master/LICENSE.txt");
        mOpenSources.add(os0);

        OpenSource os1 = new OpenSource();
        os1.setTitle("CircleProgressbar");
        os1.setDetail("CircleProgressBar继承ProgressBar, 是包含实心和线条两种风格的圆环进度条. 此外, 进度值可以随意定制. ");
        os1.setUrl("https://github.com/dinuscxj/CircleProgressBar");
        mOpenSources.add(os1);

    }
}
