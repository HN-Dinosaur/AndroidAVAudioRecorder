package com.example.myavaudiorecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;


import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.example.myavaudiorecorder.Model.Constant;
import com.example.myavaudiorecorder.Model.Info;
import com.example.myavaudiorecorder.ViewCotroller.GetBase64;
import com.example.myavaudiorecorder.ViewCotroller.HttpRequest;

import java.io.IOException;
import java.net.HttpRetryException;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    private RecordButton recordButton = null;
    private MediaRecorder recorder = null;

    private PlayButton   playButton = null;
    private MediaPlayer   player = null;

    private MyText myText = null;

    Runnable runnable = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};


    //界面布局
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //录音文件地址
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.m4a";
        //页面布局
        settingLayout();



        runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    //请求token
                    String tokenResult = HttpRequest.getToken(Constant.tokenURL);
                    //拿到token
                    String token = HttpRequest.processJSONForToken(tokenResult);
                    //封装成一个类
                    Info infoClass = new Info(HttpRequest.trimSpaceTag(GetBase64.encodeBase64File(fileName)),
                            HttpRequest.getFileLen(fileName),token);
//                    //请求info
                    String infoResult = null;
                    infoResult = HttpRequest.getResult(Constant.infoURL,infoClass);
                    //处理result的json
                    String result = HttpRequest.processJSONForResult(infoResult);
                    myText.setText(result);
                } catch (HttpRetryException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };



    }
    //权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();

        }
    }
    //开始播放
    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }
    //停止播放
    private void stopPlaying() {
        player.release();
        player = null;

    }
    //开始录音
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(fileName);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioChannels(1);
        recorder.setAudioSamplingRate(16000);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }
    //停止录音
    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
        myText.setText("请稍等,正在识别");
        //开始语音识别
        Thread thread = new Thread(runnable);
        thread.start();
    }





    //界面布局
    @SuppressLint("WrongConstant")
    public void settingLayout(){
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(1);
        ll.setPadding(10,10,10,10);
        recordButton = new RecordButton(this);
        ll.addView(recordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        playButton = new PlayButton(this);
        ll.addView(playButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        myText = new MyText(this);
        myText.setText("请点击Start录音");
//        myText.setBackgroundColor(Color.BLACK);
        myText.setHeight(1800);
        myText.setWidth(1600);
        ll.addView(myText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0));
        setContentView(ll);
    }



    class MyText extends androidx.appcompat.widget.AppCompatTextView{

        public MyText(@NonNull Context context) {
            super(context);
        }
    }
    class RecordButton extends androidx.appcompat.widget.AppCompatButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends androidx.appcompat.widget.AppCompatButton {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }


    }
}