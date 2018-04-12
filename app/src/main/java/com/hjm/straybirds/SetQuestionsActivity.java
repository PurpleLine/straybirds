package com.hjm.straybirds;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.hjm.straybirds.tools.BaseActivity;
import com.hjm.straybirds.tools.StatusBarUtils;

public class SetQuestionsActivity extends BaseActivity {

    private Spinner mSpinner1;
    private Spinner mSpinner2;
    private Spinner mSpinner3;
    private EditText mEtCustom1;
    private EditText mEtCustom2;
    private EditText mEtCustom3;
    private EditText mEtAnswer1;
    private EditText mEtAnswer2;
    private EditText mEtAnswer3;
    private Button mBtSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_questions);
        StatusBarUtils.translucent(this);
    }

    private void bindView() {

    }

    private void viewSetEvent(){}
}
