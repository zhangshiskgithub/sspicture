package com.cpx.sspicture;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

/**
 * desc: <br>
 * author by zsq <br>
 * create on 2022-02-18 17:46<br>
 */
public class VideoViewActivity extends AppCompatActivity {
    private static final String EXTRA_KEY_PATH="path";
    private VideoView vv_video;
    private MediaController mController;
    private LinearLayout ll_back;
    private FrameLayout fl_root;
    public static void startPage(Activity activity, String path){
        Intent intent = new Intent(activity, VideoViewActivity.class);
        intent.putExtra(EXTRA_KEY_PATH,path);
        activity.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        fl_root = (FrameLayout) findViewById(R.id.fl_root);
        ll_back = (LinearLayout) findViewById(R.id.ll_back);
        vv_video = (VideoView) findViewById(R.id.videoView);
        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 实例化MediaController
        mController = new MediaController(this);
        String path = getIntent().getStringExtra(EXTRA_KEY_PATH);
        File file = new File(path);
        if (file.exists()) {
            // 设置播放视频源的路径
            vv_video.setVideoPath(file.getAbsolutePath());
            // 为VideoView指定MediaController
            vv_video.setMediaController(mController);
            // 为MediaController指定控制的VideoView
            mController.setMediaPlayer(vv_video);
            vv_video.start();
            vv_video.requestFocus();
        }
        fl_root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.show();
            }
        });
    }
}
