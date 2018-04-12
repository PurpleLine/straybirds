package com.hjm.straybirds;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hjm.mylibrary.CopyRefreshLayout;
import com.hjm.straybirds.model.Diary;
import com.hjm.straybirds.model.DiaryManager;
import com.hjm.straybirds.tools.BaseActivity;
import com.hjm.straybirds.tools.StatusBarUtils;
import com.hjm.straybirds.tools.ToastTool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private Toolbar mToolbar;
    private CopyRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mToggle;
    private SearchView mSearchView;
    private DiaryManager mDiaryManager;
    private TextView mTvNoContent;
    private List<Diary> mList;
    private Menu mMenu;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtils.translucent(this);

        bindView();
        viewSetEvent();
    }

    private void bindView() {
        mToolbar = findViewById(R.id.activity_main_draw_toolbar);
        mRefreshLayout = findViewById(R.id.activity_main_draw_crl);
        mRecyclerView = findViewById(R.id.activity_main_draw_rv);
        mDrawerLayout = findViewById(R.id.activity_main_drawer);
        mNavigationView = findViewById(R.id.activity_main_navigation);
        mTvNoContent = findViewById(R.id.activity_main_tv_no_content);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.activity_main_toolbar_drawer_open, R.string.activity_main_toolbar_drawer_close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        mDiaryManager = DiaryManager.getInstance(this);
        mList = mDiaryManager.initialDiary();

        MyAdapter adapter = new MyAdapter(mList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayout.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(adapter);

        mRefreshLayout.setColorSchemeResources(R.color.selectColor);
    }

    private void viewSetEvent() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {
                    mRefreshLayout.setRefreshing(false);
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    ToastTool.toShow("加载成功");
                } else {
                    mRefreshLayout.setRefreshing(false);
                    ToastTool.toShow("没有可加载数据");
                }
                return true;
            }
        });

        mRefreshLayout.setOnRefreshListener(new CopyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDiaryManager.queryNewDiary()) {
                            mHandler.sendEmptyMessage(1);
                        } else {
                            mHandler.sendEmptyMessage(-1);
                        }
                    }
                }).start();
            }
        });

        mRefreshLayout.setOnBottomRefreshListener(new CopyRefreshLayout.OnBottomRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDiaryManager.queryOldDiary()) {
                            mHandler.sendEmptyMessage(1);
                        } else {
                            mHandler.sendEmptyMessage(-1);
                        }
                    }
                }).start();
            }
        });

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.main_nav_pictures: {
                        Intent intent = new Intent(MainActivity.this, PictureStorageActivity.class);
                        startActivity(intent);
//                        mMenu.findItem(R.id.activity_main_tb_new).setEnabled(false);
//                        mMenu.findItem(R.id.activity_main_tb_new).setIcon(R.drawable.activity_edit_tb_save);
                    }
                    break;
                    case R.id.main_nav_statistics: {
//                        mMenu.findItem(R.id.activity_main_tb_new).setEnabled(true);
//                        mMenu.findItem(R.id.activity_main_tb_new).setIcon(R.drawable.activity_main_tb_new);
                    }
                    break;
                    default:
                }

                mDrawerLayout.closeDrawer(Gravity.START);
                return true;
            }
        });
    }

    private class MyHolder extends RecyclerView.ViewHolder {

        private RelativeLayout mRlayout;
        private TextView mTvDay;
        private TextView mTvMonth;
        private TextView mTvTitle;
        private TextView mTvContent;
        private TextView mTvCity;
        private TextView mTvTime;
        private TextView mTvWeather;
        private ImageView mIvMood;
        private SimpleDateFormat mFormat;
        private int mIndex;

        public MyHolder(View itemView) {
            super(itemView);
            mRlayout = itemView.findViewById(R.id.main_crl_item_rl);
            mFormat = new SimpleDateFormat("dd-/MM-EE/HH:mm", Locale.CHINA);
            mTvDay = itemView.findViewById(R.id.main_crl_item_tv_day);
            mTvMonth = itemView.findViewById(R.id.main_crl_item_tv_month);
            mTvTitle = itemView.findViewById(R.id.main_crl_item_tv_title);
            mTvContent = itemView.findViewById(R.id.main_crl_item_tv_content);
            mTvCity = itemView.findViewById(R.id.main_crl_item_tv_city);
            mTvTime = itemView.findViewById(R.id.main_crl_item_tv_time);
            mTvWeather = itemView.findViewById(R.id.main_crl_item_tv_weather);
            mIvMood = itemView.findViewById(R.id.main_crl_item_iv_mood);

            mRlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra("PAGE_INDEX", mIndex);
                    startActivity(intent);
                }
            });
        }

        private String[] dateFormat(Date date) {
            return mFormat.format(date).split("-");
        }

        public void viewBindDiary(Diary diary, int index) {
            String[] date = dateFormat(diary.getDate());
            mIndex = index;
            mIvMood.setImageDrawable(getResources().getDrawable(R.drawable.mood_icon));
            mIvMood.setImageLevel(diary.getMood());
            mTvDay.setText(date[0]);
            mTvMonth.setText(date[1]);
            mTvTime.setText(date[2]);
            mTvWeather.setText(diary.getWeather());
            mTvCity.setText(diary.getCity());
            mTvContent.setText(diary.getContentText());
            mTvTitle.setText(diary.getTitle());
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> implements Filterable {

        private List<Diary> mDiaries;

        public MyAdapter(List<Diary> diaries) {
            mDiaries = diaries;
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_main_crl_item,
                    parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            holder.viewBindDiary(mDiaries.get(position), position);
        }

        @Override
        public int getItemCount() {
            return mDiaries.size();
        }

        @Override
        public Filter getFilter() {
            return new MyFilter(this, (mDiaryManager.getBakDiaries() != null) ? mDiaryManager.getBakDiaries() : mDiaryManager.getDiaries());
        }

        private class MyFilter extends Filter {
            final MyAdapter mAdapter;
            final List<Diary> mOriginalList;
            final List<Diary> mFilterList;

            public MyFilter(MyAdapter adapter, List<Diary> originalList) {
                mAdapter = adapter;
                mOriginalList = originalList;
                mFilterList = new ArrayList<>();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                mFilterList.clear();
                if (constraint == null || TextUtils.isEmpty(constraint)) {
                    mFilterList.addAll(mOriginalList);
                } else {
                    for (Diary d : mOriginalList) {
                        if (d.getTitle().contains(constraint) || d.getContentText().contains(constraint)) {
                            mFilterList.add(d);
                        }
                    }
                }

                results.values = mFilterList;
                results.count = mFilterList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mDiaryManager.setDiaries(mFilterList);
                mAdapter.notifyDataSetChanged();
            }

            public void cancelFilter() {
                mDiaryManager.restoreDiaries();
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_tb_menu, menu);
        mMenu = menu;
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.activity_main_tb_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setIconified(true);
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                ((MyAdapter.MyFilter) (((MyAdapter) mRecyclerView.getAdapter()).getFilter())).cancelFilter();
                mRefreshLayout.setEnabled(true);
                return false;
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ((MyAdapter) (mRecyclerView.getAdapter())).getFilter().filter(newText);
                return true;
            }
        });
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRefreshLayout.setEnabled(false);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_main_tb_new: {
                Intent intent = new Intent(this, EditActivity.class);
                startActivity(intent);
                return true;
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mList.size() == 0) {
            mTvNoContent.setVisibility(View.VISIBLE);
        } else {
            mTvNoContent.setVisibility(View.GONE);
        }
        Collections.sort(mList);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }
}
