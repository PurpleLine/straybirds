package com.hjm.straybirds;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.hjm.straybirds.model.PictureLists;
import com.hjm.straybirds.tools.LogTool;
import com.hjm.straybirds.tools.PictureUtils;
import com.hjm.straybirds.tools.ToastTool;

import java.io.File;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.UploadFileListener;

public class BmobTestActivity extends AppCompatActivity {

    private int id = 0;
    private List<PictureLists> mPictureLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmob_test);

        //第一：默认初始化
        Bmob.initialize(this, "e905197d89bf75258d3cb0943266f884");
        // 注:自v3.5.2开始，数据sdk内部缝合了统计sdk，开发者无需额外集成，传渠道参数即可，不传默认没开启数据统计功能
        //Bmob.initialize(this, "Your Application ID","bmob");

        //第二：自v3.4.7版本开始,设置BmobConfig,允许设置请求超时时间、文件分片上传时每片的大小、文件的过期时间(单位为秒)，
        //BmobConfig config =new BmobConfig.Builder(this)
        ////设置appkey
        //.setApplicationId("Your Application ID")
        ////请求超时时间（单位为秒）：默认15s
        //.setConnectTimeout(30)
        ////文件分片上传时每片的大小（单位字节），默认512*1024
        //.setUploadBlockSize(1024*1024)
        ////文件的过期时间(单位为秒)：默认1800s
        //.setFileExpiration(2500)
        //.build();
        //Bmob.initialize(config);


        Button commit = findViewById(R.id.activity_bmob_test_bt);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PictureLists pictureLists = new PictureLists();
                String name = "sdl_log.txt";
                pictureLists.setName(name);
                String path = PictureUtils.getPathViaName(BmobTestActivity.this, name);
                BmobFile file = new BmobFile(new File(path));
                pictureLists.setContent(new BmobFile(name, "test", file.getUrl()));
//                pictureLists.setContent(file);

                file.uploadblock(new UploadFileListener() {
                    @Override
                    public void done(BmobException e) {
                        LogTool.log("test", file.getFileUrl(), LogTool.INFO);
                        LogTool.log("test", file.getUrl(), LogTool.INFO);
                    }


                });

//                pictureLists.save(new SaveListener<String>() {
//                    @Override
//                    public void done(String s, BmobException e) {
//                        if (e == null) {
//                            ToastTool.toShow("提交成功");
//                        } else {
//                            ToastTool.toShow("提交失败");
//                        }
//                    }
//                });
            }
        });

        Button download = findViewById(R.id.activity_bmob_test_bt1);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                BmobQuery<PictureLists> query = new BmobQuery<PictureLists>();
////查询playerName叫“比目”的数据
////                query.addWhereEqualTo("playerName", "比目");
////返回50条数据，如果不加上这条语句，默认返回10条数据
//                query.setLimit(50);
////执行查询方法
//                query.findObjects(new FindListener<PictureLists>() {
//                    @Override
//                    public void done(List<PictureLists> object, BmobException e) {
//                        if(e==null){
//                            mPictureLists = object;
//                            ToastTool.toShow("查询成功：共"+object.size()+"条数据。");
//                        }else{
//                            Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
//                        }
//                    }
//                });
                String uri = "http://bmob-cdn-18041.b0.upaiyun.com/2018/04/12/6c60e54426784563b332993e74160f1b.txt";
                BmobFile file = new BmobFile("haha.txt", null, uri);
                downloadFile(file);

//                BmobQuery<PictureLists> query = new BmobQuery<>();
//                query.getObject("38beae8548", new QueryListener<PictureLists>() {
//                    @Override
//                    public void done(PictureLists object, BmobException e) {
//                        if (e == null) {
//                            BmobFile file = object.getContent();
//                            downloadFile(file);
//                        } else {
//                            Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
//                        }
//                    }
//                });

            }
        });

        Button debug = findViewById(R.id.activity_bmob_test_bt2);
        debug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id++;
            }
        });
    }

    private void downloadFile(BmobFile file) {
        //允许设置下载文件的存储路径，默认下载文件的目录为：context.getApplicationContext().getCacheDir()+"/bmob/"
        File saveFile = new File(Environment.getExternalStorageDirectory(), file.getFilename());
        file.download(saveFile, new DownloadFileListener() {

            @Override
            public void onStart() {
                ToastTool.toShow("开始下载...");
            }

            @Override
            public void done(String savePath, BmobException e) {
                if (e == null) {
                    ToastTool.toShow("下载成功,保存路径:" + savePath);
                } else {
                    ToastTool.toShow("下载失败：" + e.getErrorCode() + "," + e.getMessage());
                }
            }

            @Override
            public void onProgress(Integer value, long newworkSpeed) {
                Log.i("bmob", "下载进度：" + value + "," + newworkSpeed);
            }

        });
    }
}
