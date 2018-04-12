package com.hjm.straybirds;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hjm.straybirds.model.Diary;
import com.hjm.straybirds.model.DiaryManager;
import com.hjm.straybirds.tools.BaseActivity;
import com.hjm.straybirds.tools.StatusBarUtils;

/**
 * Created by hejunming on 2018/3/28.
 */

public class DetailActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ViewPager mPager;
    private DiaryManager mDiaryManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        StatusBarUtils.translucent(this);

        bindView();
    }

    private void bindView() {
        mToolbar = findViewById(R.id.activity_details_toolbar);
        mPager = findViewById(R.id.activity_details_vp);
        setSupportActionBar(mToolbar);
        mDiaryManager = DiaryManager.getInstance(this);

        FragmentStatePagerAdapter adapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mDiaryManager.getDiaries().size();
            }

            @Override
            public Fragment getItem(int position) {
                Bundle bundle = new Bundle();
                bundle.putInt(DetalisFragment.ARG_INDEX, position);
                DetalisFragment fragments = new DetalisFragment();
                fragments.setArguments(bundle);
                return fragments;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };
        mPager.setAdapter(adapter);
        int index = getIntent().getExtras().getInt("PAGE_INDEX");
        if (index >= 0) {
            mPager.setCurrentItem(index);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_detail_tb_menu_, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_detail_tb_menu_edit: {
                Intent intent = new Intent(this, EditActivity.class);
                intent.putExtra("EDIT_INDEX", mPager.getCurrentItem());
                startActivityForResult(intent, 1);
                return true;
            }
            case R.id.activity_detail_tb_menu_delete: {
                Snackbar snackbar = Snackbar.make(mToolbar, "确定删除该条日记吗?", Snackbar.LENGTH_LONG);
                snackbar.setAction("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteDiary(mDiaryManager.getDiaries().get(mPager.getCurrentItem()));
                    }
                });
                snackbar.show();
                return true;
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteDiary(Diary diary) {
        mDiaryManager.deleteDiary(getApplicationContext(), diary);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            int index = data.getIntExtra("EDIT_INDEX", 0);
            if (index != -1) {
                mPager.getAdapter().notifyDataSetChanged();
                mPager.setCurrentItem(index);
            } else {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
