package com.topdon.lib.ui.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.topdon.lib.core.ktbase.BaseFragment;
import com.topdon.lib.ui.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class PageFragment extends BaseFragment {


    public static PageFragment newInstance(int res) {
        PageFragment fragmentFirst = new PageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("res",res);
        fragmentFirst.setArguments(bundle);
        return fragmentFirst;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageView = view.findViewById(R.id.img);
        int res = getArguments().getInt("res");
        imageView.setImageResource(res);
    }

    @Override
    public int initContentView() {
        return R.layout.fragment_page;
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {

    }
}
