package com.hjm.straybirds;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hjm.straybirds.tools.BaseActivity;
import com.hjm.straybirds.tools.StatusBarUtils;
import com.hjm.straybirds.tools.ToastTool;
import com.hjm.straybirds.tools.Validator;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by hejunming on 2018/3/21.
 */

public class LoginActivity extends BaseActivity {

    private CircleImageView mCivIcon;
    private TextView mTvSetQuestion;
    private TextView mTvFindPassword;
    private EditText mEtUserName;
    private EditText mEtPassword;
    private Button mBtLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarUtils.translucent(this);

        viewBind();
        viewSetEvent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void viewBind() {
        mCivIcon = findViewById(R.id.activity_login_iv_icon);
        mTvSetQuestion = findViewById(R.id.activity_login_tv_questions);
        mTvFindPassword = findViewById(R.id.activity_login_tv_find_password);
        mEtPassword = findViewById(R.id.activity_login_et_password);
        mEtUserName = findViewById(R.id.activity_login_et_username);
        mBtLogin = findViewById(R.id.activity_login_bt_login);
    }

    private void viewSetEvent() {
        mTvSetQuestion.setTextColor(getResources().getColor(R.color.colorPrimary));
        mTvSetQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开问题设置页面
                Intent intent = new Intent(LoginActivity.this, SetQuestionsActivity.class);
                startActivity(intent);
            }
        });

        mTvFindPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开密码找回页面
            }
        });

        mBtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = mEtUserName.getText().toString();
                String password = mEtPassword.getText().toString();

                if (!Validator.isUserName(name)) {
                    ToastTool.toShow("用户名格式错误");

                } else if (!Validator.isPassword(password)) {
                    ToastTool.toShow("密码格式错误");
                } else {
                    //进入主界面
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }

            }
        });
    }
}
