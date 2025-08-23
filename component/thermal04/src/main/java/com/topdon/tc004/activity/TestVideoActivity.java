package com.topdon.tc004.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.topdon.tc004.R;
import com.topdon.tc004.activity.video.PlayFragment;

public class TestVideoActivity extends AppCompatActivity {

    private PlayFragment mRenderFragment;
    private String url = "rtsp://192.168.40.1/stream0";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_video);
        int transportMode = 1;
        int sendOption = 1;
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
                .putBoolean("use-sw-codec", true)
                .apply();
        mRenderFragment = PlayFragment.newInstance(url,transportMode,sendOption,null);
        getSupportFragmentManager().beginTransaction().add(R.id.render_holder, mRenderFragment).commit();
    }
}
