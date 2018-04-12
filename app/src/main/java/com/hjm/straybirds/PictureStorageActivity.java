package com.hjm.straybirds;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.hjm.straybirds.model.Picture;
import com.hjm.straybirds.tools.LruCacheUtils;
import com.hjm.straybirds.tools.PictureUtils;
import com.hjm.straybirds.tools.StatusBarUtils;
import com.hjm.straybirds.tools.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class PictureStorageActivity extends AppCompatActivity {

    private static final String TAG = "PictureStorageActivity";
    private Toolbar mToolbar;
    private GridView mGridView;
    private List<Picture> mShowPic;
    private String[] mAllPic;
    private Handler mHandler;
    private MyAsyncTask mRefreshPic;
    private int mStartIndex = 0;
    private int mEndIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_storage);
        StatusBarUtils.translucent(this);

        mToolbar = findViewById(R.id.activity_picture_storage_tb);
        mGridView = findViewById(R.id.activity_picture_storage_gv);
        setSupportActionBar(mToolbar);

        mShowPic = new ArrayList<>();
        mEndIndex = ViewUtils.getWindowPxy()[1] / ViewUtils.dip2px(80) * 4 + mStartIndex;
        final int initialLoadCount = mEndIndex - mStartIndex;

        MyAdapter adapter = new MyAdapter(mShowPic);
        mGridView.setAdapter(adapter);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
//                    Log.i(TAG, "onScrollStateChanged: " + "SCROLL_STATE_IDLE");
                    if (!mRefreshPic.isCancelled()) {
                        mRefreshPic.cancel(true);
                    }
                    mRefreshPic = new MyAsyncTask();
                    String[] buf = new String[mEndIndex - mStartIndex];
                    System.arraycopy(mAllPic, mStartIndex, buf, 0, mEndIndex - mStartIndex);
                    mRefreshPic.execute(buf);
                } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
//                    Log.i(TAG, "onScrollStateChanged: " + "SCROLL_STATE_TOUCH_SCROLL");
                } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
//                    Log.i(TAG, "onScrollStateChanged: " + "SCROLL_STATE_FLING");
                    if (!mRefreshPic.isCancelled()) {
                        mRefreshPic.cancel(true);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                Log.i(TAG, "onScroll: " + "[" + firstVisibleItem + " " + visibleItemCount + " " + totalItemCount + "]");
                mStartIndex = firstVisibleItem;
                mEndIndex = mStartIndex + visibleItemCount;
            }
        });

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PictureStorageActivity.this, BigPictureActivity.class);
                intent.putExtra("ALL_IMAGE_NAME", mAllPic);
                intent.putExtra("CURRENT_INDEX", position);
                startActivity(intent);
            }
        });


        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在加载中...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                mAllPic = msg.getData().getStringArray("ALL_PICTURE_NAME");
                if (mAllPic != null) {
                    for (String s : mAllPic) {
                        mShowPic.add(new Picture(s));
                    }
                    ((BaseAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                    progressDialog.cancel();
                    mRefreshPic = new MyAsyncTask();
                    if (mShowPic.size() < initialLoadCount) {
                        mRefreshPic.execute(mAllPic);
                    } else {
                        String[] sub = new String[initialLoadCount];
                        System.arraycopy(mAllPic, 0, sub, 0, initialLoadCount);
                        mRefreshPic.execute(sub);
                    }
                }
                return true;
            }
        });
        Thread loadAllPicture = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] result = PictureUtils.getDefPathAllPicture(getApplicationContext());
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putStringArray("ALL_PICTURE_NAME", result);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        });
        loadAllPicture.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mRefreshPic.isCancelled()) {
            mRefreshPic.cancel(true);
        }
    }

    private class MyAdapter extends BaseAdapter {
        private List<Picture> mList;

        public MyAdapter(List<Picture> list) {
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_picture_storage_gv_item, parent, false);
                holder = new MyHolder();
                holder.mImageView = convertView.findViewById(R.id.activity_picture_storage_gv_item_iv);
                convertView.setTag(holder);
            } else {
                holder = (MyHolder) convertView.getTag();
            }
            Bitmap bm = LruCacheUtils.getInstance().getSmallBitmapFromCache(mList.get(position).getName());
            if (bm != null) {
                holder.mImageView.setImageBitmap(bm);
            } else {
                holder.mImageView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.mood_sad));
                mList.get(position).setInvalid(false);
            }
            Log.i(TAG, "getView: " + position);
            return convertView;
        }

        private class MyHolder {
            public ImageView mImageView;
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            ((BaseAdapter) mGridView.getAdapter()).notifyDataSetChanged();
        }

        @Override
        protected Integer doInBackground(String... string) {
            for (int i = 0; i < string.length; i++) {
                if (isCancelled()) {
                    return i;
                }
                if (LruCacheUtils.getInstance().loadSmallPicToCache(getApplicationContext(), string[i])) {
                    mShowPic.get(mStartIndex + i).setInvalid(true);
                }
            }
            return string.length;
        }
    }
}
