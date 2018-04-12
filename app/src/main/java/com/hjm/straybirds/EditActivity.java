package com.hjm.straybirds;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hjm.straybirds.model.Diary;
import com.hjm.straybirds.model.DiaryContent;
import com.hjm.straybirds.model.DiaryManager;
import com.hjm.straybirds.service.MyIntentService;
import com.hjm.straybirds.tools.BaseActivity;
import com.hjm.straybirds.tools.LruCacheUtils;
import com.hjm.straybirds.tools.PermissionUtils;
import com.hjm.straybirds.tools.PictureUtils;
import com.hjm.straybirds.tools.StatusBarUtils;
import com.hjm.straybirds.tools.ToastTool;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by hejunming on 2018/3/28.
 */

public class EditActivity extends BaseActivity {

    private static final int IS_NEW = 0;
    private static final int IS_ALTER = 1;

    private Toolbar mToolbar;
    private EditText mEtTitle;
    private TextView mTvCity;
    private TextView mTvWeather;
    private TextView mTvMood;
    private RecyclerView mRvContent;

    private DiaryManager mDiaryManager;
    private Diary mDiary;
    private Diary mSaveDiary;
    private List<DiaryContent> mList;
    private LruCacheUtils mCacheUtils;
    private String[] mMoodArray;
    private List<String> mPictureAddRecord;
    private int NEW_OR_ALTER;

    //辅助操作图文混排
    private TextView mRvFocusEt;
    private int mRvFocusItem = 0;
    private int mRvEtSelectorStart = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        StatusBarUtils.translucent(this);

        judgeNewOrJustAlter();
        bindView();
        setViewEvent();
    }

    private void setResultToBackActivity(int index) {
        Intent result = new Intent();
        result.putExtra("EDIT_INDEX", index);
        setResult(RESULT_OK, result);
    }

    private void judgeNewOrJustAlter() {
        mDiaryManager = DiaryManager.getInstance(this);
        mMoodArray = getResources().getStringArray(R.array.activity_edit_mood_dialog_item);
        mCacheUtils = LruCacheUtils.getInstance();
        mPictureAddRecord = new ArrayList<>();

        int index = getIntent().getIntExtra("EDIT_INDEX", -1);
        if (index == -1) {
            //新建日记
            mDiary = new Diary();
            mList = mDiary.getContents();
            mSaveDiary = new Diary(mDiary);
            NEW_OR_ALTER = IS_NEW;
        } else {
            //修改日记
            mDiary = new Diary(mDiaryManager.getDiaries().get(index));
            mList = mDiary.getContents();
            mSaveDiary = new Diary(mDiary);
            setResultToBackActivity(index);
            mPictureAddRecord.addAll(Arrays.asList(mDiary.getContentImgs()));
            NEW_OR_ALTER = IS_ALTER;
        }
    }

    private boolean diaryKeyContentIsEmpty(Diary diary) {
        return TextUtils.isEmpty(diary.getTitle())
                && TextUtils.isEmpty(diary.getContentText())
                && diary.getContentImgs().length == 0;
    }

    private void saveEditDiary(Diary diary, boolean toShow) {
        if (diary == null) {
            return;
        }

        if (diaryKeyContentIsEmpty(diary)) {
            if (toShow) {
                ToastTool.toShow("基本信息为空,保存无效");
            }
        } else {
            if (!mSaveDiary.allContentEqualsTo(diary)) {
                mSaveDiary.clear();
                mSaveDiary = null;
                mSaveDiary = new Diary(diary);
                mSaveDiary.setDate(new Date());
                mDiaryManager.commitDiary(mSaveDiary);
            }
            if (toShow) {
                ToastTool.toShow("保存成功");
            }
        }
    }

    private void bindView() {
        mToolbar = findViewById(R.id.activity_edit_toolbar);
        mEtTitle = findViewById(R.id.activity_edit_et_title);
        mTvCity = findViewById(R.id.activity_edit_tv_city);
        mTvWeather = findViewById(R.id.activity_edit_tv_weather);
        mTvMood = findViewById(R.id.activity_edit_tv_mood);
        mRvContent = findViewById(R.id.activity_edit_content_rv);
        setSupportActionBar(mToolbar);
    }

    private void setViewEvent() {
        //填充内容
        mEtTitle.setText(mDiary.getTitle());
        mTvCity.setText("地点:" + mDiary.getCity());
        mTvWeather.setText("天气:" + mDiary.getWeather());
        mTvMood.setText("心情:" + mMoodArray[mDiary.getMood()]);
        MyAdapter adapter = new MyAdapter(mList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayout.VERTICAL, false);
        mRvContent.setAdapter(adapter);
        mRvContent.setLayoutManager(layoutManager);
        //事件刷新内容
        mEtTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mDiary.setTitle(s.toString());
            }
        });
        mTvMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder chooserDialog = new AlertDialog.Builder(EditActivity.this);
                chooserDialog.setTitle(getResources().getString(R.string.activity_edit_mood_dialog_title));
                chooserDialog.setItems(R.array.activity_edit_mood_dialog_item, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDiary.setMood(which);
                        mTvMood.setText("心情:" + mMoodArray[which]);
                    }
                });
                chooserDialog.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.acticity_edit_tb_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_edit_tb_menu_insert_picture: {
                if (getCurrentFocus().getId() == R.id.activity_edit_rv_item_text) {
                    //WRITE_EXTERNAL_STORAGE 在开启应用时就必须申请
                    if (PermissionUtils.checkPromission(this, Manifest.permission.CAMERA)) {
                        PictureUtils.showChooseDialog(this, PictureUtils.generatePicName(mDiary.getId()), true);
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                    }
                } else {
                    ToastTool.toShow("此处不能插入图片");
                }
                return true;
            }
            case R.id.activity_edit_tb_menu_save_content: {
                saveEditDiary(mDiary, true);
                return true;
            }
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            PictureUtils.showChooseDialog(this, PictureUtils.generatePicName(mDiary.getId()), true);
        } else {
            PictureUtils.showChooseDialog(this, PictureUtils.generatePicName(mDiary.getId()), false);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void deletePictureBeforeFinish() {
        List<String> needToDelete = new ArrayList<>();
        List<String> saveImgs = new ArrayList<>();
        saveImgs.addAll(Arrays.asList(mSaveDiary.getContentImgs()));
        for (String s: mPictureAddRecord) {
            if (!saveImgs.contains(s)) {
                needToDelete.add(s);
            }
        }
        //mCacheUtils.deleteBitmap(this, needToDelete.toArray(new String[needToDelete.size()]));
        Intent intent = new Intent(getApplicationContext(), MyIntentService.class);
        intent.putExtra(MyIntentService.EXTRA_KEY, MyIntentService.DELETE_IMG_NAMES);
        intent.putExtra(MyIntentService.DELETE_IMG_NAMES, needToDelete.toArray(new String[needToDelete.size()]));
        startService(intent);
    }

    @Override
    public void onBackPressed() {
        if (diaryKeyContentIsEmpty(mDiary)) {
            //基本信息为空,询问是否删除该条日记
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("提醒:");
            dialog.setMessage("基本信息为空,是否删除该条日记?");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDiaryManager.deleteDiary(getApplicationContext(), mDiary);
                    mDiary.clear();
                    setResultToBackActivity(-1);
                    deletePictureBeforeFinish();
                    finish();
                }
            });
            dialog.show();
        } else if (!mDiary.allContentEqualsTo(mSaveDiary)) {
            //日记已经改动过,询问是否保存变动内容
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("提醒:");
            dialog.setMessage("是否保存修改后的内容并退出?");
            dialog.setPositiveButton("保存后退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveEditDiary(mDiary, true);
                    mDiary.clear();
                    deletePictureBeforeFinish();
                    finish();
                }
            });
            dialog.setNegativeButton("不保存退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveEditDiary(mSaveDiary, false);
                    mDiary.clear();
                    deletePictureBeforeFinish();
                    finish();
                }
            });
            dialog.show();
        } else {
            //日记已经保存,直接退出
            mDiary.clear();
            deletePictureBeforeFinish();
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureUtils.CHOOSE_PICTURE_FROM_ALBUM: {
                    String dstPath = PictureUtils.getDefaultDiaryPicturePath(this) + File.separator + PictureUtils.getPicName();
                    PictureUtils.compressPicture(this, data.getData(), dstPath);
                    showChoosePicture();
                }
                break;
                case PictureUtils.CHOOSE_PICTURE_FROM_CAMERA: {
                    String srcPath = PictureUtils.getDefaultDiaryPicturePath(this) + File.separator + PictureUtils.getPicName();
                    PictureUtils.compressPicture(srcPath, srcPath);
                    showChoosePicture();
                }
                break;
                default:
            }
        }
    }

    private void showChoosePicture() {
        mRvEtSelectorStart = mRvFocusEt.getSelectionStart();
        String string = mList.get(mRvFocusItem).getTextContent().toString();
        String stringLast = string.substring(0, mRvEtSelectorStart);
        String stringNext = string.substring(mRvEtSelectorStart, string.length());
        mList.get(mRvFocusItem).getTextContent().replace(0, string.length(), stringLast);

        DiaryContent content = new DiaryContent();
        content.getTextContent().append(stringNext);
        content.setImageName(PictureUtils.getPicName());
        mList.add(mRvFocusItem + 1, content);
        mRvContent.getAdapter().notifyDataSetChanged();
        //记录所有添加过的图片
        mPictureAddRecord.add(PictureUtils.getPicName());
    }

    private class MyHolder extends RecyclerView.ViewHolder {

        private ImageView mImageView;
        private EditText mEditText;
        private Bitmap mBitmap;
        private int mCurrentIndex;

        public MyHolder(View itemView) {
            super(itemView);
            mEditText = itemView.findViewById(R.id.activity_edit_rv_item_text);
            mImageView = itemView.findViewById(R.id.activity_edit_rv_item_image);

            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    int length = mList.get(mCurrentIndex).getTextContent().length();
                    mList.get(mCurrentIndex).getTextContent().replace(0, length, s.toString());
                }
            });
            mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    mRvFocusEt = mEditText;
                    mRvFocusItem = mCurrentIndex;
                }
            });
            mImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(EditActivity.this);
                    dialog.setItems(new String[]{"删除该图片"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //这里只删除图片名字,退出时才去真正删除多余图片
                            mList.get(mCurrentIndex).setImageName(null);
                            //如果被移除图片的不是第一个item,则把该item剩下的文本内容拼接到上一个item的末尾
                            if (mCurrentIndex != 0) {
                                String text = mList.get(mCurrentIndex).getTextContent().toString();
                                mList.get(mCurrentIndex - 1).getTextContent().append(text);
                                mList.remove(mCurrentIndex);
                            }
                            //通知适配器数据发生改变
                            mRvContent.getAdapter().notifyDataSetChanged();
                        }
                    });
                    dialog.show();
                    return true;
                }
            });
        }

        public void onBind(DiaryContent diaryContent, int index) {
            //保存辅助信息
            mCurrentIndex = index;

            String content = diaryContent.getTextContent().toString();
            String picName = diaryContent.getImageName();
            mEditText.setText(content);
            if (TextUtils.isEmpty(picName)) {
                mImageView.setVisibility(View.GONE);
                if (mBitmap != null) {
                    mBitmap = null;
                }
            } else {
                mImageView.setVisibility(View.VISIBLE);
                try {
                    mBitmap = mCacheUtils.loadBitmap(EditActivity.this, picName, LruCacheUtils.NORMAL_PICTURE);
                    if (mBitmap == null) {
                        mImageView.setImageDrawable(getResources().getDrawable(R.drawable.mood_sad));
                    } else {
                        mImageView.setImageBitmap(mBitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        private List<DiaryContent> mDiaries;

        public MyAdapter(List<DiaryContent> diaries) {
            mDiaries = diaries;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplication()).inflate(R.layout.activity_edit_rv_item, parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            holder.onBind(mDiaries.get(position), position);
        }

        @Override
        public int getItemCount() {
            return mDiaries.size();
        }
    }
}
