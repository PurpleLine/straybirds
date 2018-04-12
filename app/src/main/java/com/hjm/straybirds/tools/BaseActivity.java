package com.hjm.straybirds.tools;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hjm.straybirds.R;

/**
 * Created by hejunming on 2018/4/7.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.activity_test_1, R.anim.activity_test_2);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_test_3, R.anim.activity_test_4);
    }
}
