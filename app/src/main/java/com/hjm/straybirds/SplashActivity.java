package com.hjm.straybirds;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.WindowManager;

import com.hjm.straybirds.tools.BaseActivity;
import com.hjm.straybirds.tools.PermissionUtils;
import com.hjm.straybirds.tools.ToastTool;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hejunming on 2018/3/20.
 */

public class SplashActivity extends BaseActivity {

    private Timer mTimer = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
        setContentView(R.layout.activity_splash);

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (PermissionUtils.checkPromission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    jump();
                } else {
                    ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        }, 3000);

    }

    private void jump() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTimer.cancel();
        mTimer = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            jump();
        } else {
            ToastTool.toShow("权限不足,无法启动");
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
