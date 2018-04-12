package com.hjm.straybirds;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hjm.straybirds.model.Diary;
import com.hjm.straybirds.model.DiaryContent;
import com.hjm.straybirds.model.DiaryManager;
import com.hjm.straybirds.tools.LruCacheUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by hejunming on 2018/3/28.
 */

public class DetalisFragment extends Fragment {

    public static final String ARG_INDEX = "arg_index";

    private TextView mTvTitle;
    private TextView mTvDate;
    private TextView mTvWeather;
    private TextView mTvMood;
    private TextView mTvCity;
    private RecyclerView mRvContent;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private Diary mDiary;
    private DiaryManager mDiaryManager;
    private LruCacheUtils mCacheUtils;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCacheUtils = LruCacheUtils.getInstance();
        mDiaryManager = DiaryManager.getInstance(getActivity());
        Bundle bundle = getArguments();
        mDiary = mDiaryManager.getDiaries().get(bundle.getInt(ARG_INDEX));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activitu_details_viewpage_item, container, false);
        mTvTitle = view.findViewById(R.id.fragment_sub_item_tv_title);
        mTvDate = view.findViewById(R.id.fragment_sub_item_tv_date);
        mTvCity = view.findViewById(R.id.fragment_sub_item_tv_city);
        mTvMood = view.findViewById(R.id.fragment_sub_item_tv_mood);
        mTvWeather = view.findViewById(R.id.fragment_sub_item_tv_weather);
        mRvContent = view.findViewById(R.id.fragment_sub_item_rv_content);

        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false);
        mAdapter = new MyAdapter(mDiary.getContents());
        mRvContent.setLayoutManager(mLayoutManager);
        mRvContent.setAdapter(mAdapter);

        mTvTitle.setText(mDiary.getTitle());
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd  EE/HH:mm", Locale.CHINA);
        mTvDate.setText(format.format(mDiary.getDate()));
        mTvCity.setText(mDiary.getCity());
        mTvMood.setText(getResources().getStringArray(R.array.activity_edit_mood_dialog_item)[mDiary.getMood()]);
        mTvWeather.setText(mDiary.getWeather());

        return view;
    }

    private class MyHolder extends RecyclerView.ViewHolder {

        private TextView mTvText;
        private ImageView mIvImage;

        public MyHolder(View itemView) {
            super(itemView);
            mIvImage = itemView.findViewById(R.id.details_vp_rv_item_iv_image);
            mTvText = itemView.findViewById(R.id.details_vp_rv_item_tv_text);
        }

        public void onBind(DiaryContent diaryContent) {
            String text = diaryContent.getTextContent().toString();
            if (TextUtils.isEmpty(text)) {
                mTvText.setVisibility(View.GONE);
            } else {
                mTvText.setVisibility(View.VISIBLE);
                mTvText.setText(text);
            }
            if (diaryContent.getImageName() == null) {
                mIvImage.setVisibility(View.GONE);
            } else {
                mIvImage.setVisibility(View.VISIBLE);
                Bitmap bitmap = mCacheUtils.loadBitmap(getActivity(), diaryContent.getImageName(), LruCacheUtils.NORMAL_PICTURE);
                if (bitmap == null) {
                    mIvImage.setImageDrawable(getResources().getDrawable(R.drawable.mood_sad));
                } else {
                    mIvImage.setImageBitmap(bitmap);
                }
            }
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        private List<DiaryContent> mContents;

        public MyAdapter(List<DiaryContent> diaryContents) {
            mContents = diaryContents;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.details_vp_rv_item, parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            DiaryContent diaryContent = mContents.get(position);
            holder.onBind(diaryContent);
        }

        @Override
        public int getItemCount() {
            return mContents.size();
        }
    }
}
