package com.example.myavaudiorecorder;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;


import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.example.myavaudiorecorder.Model.Constant;
import com.example.myavaudiorecorder.Model.Info;
import com.example.myavaudiorecorder.Model.GetBase64;
import com.example.myavaudiorecorder.Model.HttpRequest;

import java.io.IOException;
import java.net.HttpRetryException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private MediaRecorder recorder = null;

    Runnable runnable = null;
    public static Handler handler;
    ImageButton playOrPause;
    SeekBar seekBar;
    TextView time,display;
    Button record;

    Boolean mStartRecording = true;

    AudioService.RecordControl recordControl;
    private MyServiceConn myServiceConn;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};


    //界面布局
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        //文件位置
        Constant.fileName = getExternalCacheDir().getAbsolutePath() + "/audiorecordtest.m4a";

        init();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    class MyServiceConn implements ServiceConnection {//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){

            recordControl = (AudioService.RecordControl) service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name){

        }
    }

    public void init(){
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        record = findViewById(R.id.record);
        display = findViewById(R.id.display);
        seekBar = findViewById(R.id.seekbar);
        playOrPause = findViewById(R.id.playOrPause);
        time = findViewById(R.id.time);

        playOrPause.setOnClickListener(this);
        record.setOnClickListener(this);

        runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    //请求token
                    String tokenResult = HttpRequest.getToken(Constant.tokenURL);
                    //拿到token
                    String token = HttpRequest.processJSONForToken(tokenResult);
                    //封装成一个类
                    Info infoClass = new Info(HttpRequest.trimSpaceTag(GetBase64.encodeBase64File(Constant.fileName)),
                            HttpRequest.getFileLen(Constant.fileName),token);
//                    //请求info
                    String infoResult = null;
                    infoResult = HttpRequest.getResult(Constant.infoURL,infoClass);
                    //处理result的json
                    String result = HttpRequest.processJSONForResult(infoResult);
                    //显示结果
                    display.setText(result);
                } catch (HttpRetryException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        //开启服务
        Intent intent = new Intent(MainActivity.this,AudioService.class);
        myServiceConn = new MyServiceConn();
        bindService(intent, myServiceConn, BIND_AUTO_CREATE);



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //进度条改变调用该回调
            @Override
            public void onProgressChanged(SeekBar seekBar, int process, boolean b) { }
            //进度条开始
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            //进度条停止
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();//获取seekBar的进度
                recordControl.seekTo(progress);//改变播放进度
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                Bundle bundle = msg.getData();//获取从子线程发送过来的音乐播放进度
                int duration = bundle.getInt("duration");
                int currentPosition = bundle.getInt("currentPosition");
                seekBar.setMax(duration);
                seekBar.setProgress(currentPosition);
                //歌曲总时长
                int minute = duration / 1000 / 60;
                int second = duration / 1000 % 60;
                String totalMinute = null;
                String totalSecond = null;
                String currMinute = null;
                String currSecond = null;
                if(minute < 10){//如果歌曲的时间中的分钟小于10
                    totalMinute = "0" + minute;//在分钟的前面加一个0
                }else{
                    totalMinute = minute+"";
                }
                if (second < 10){//如果歌曲中的秒钟小于10
                    totalSecond="0" + second;//在秒钟前面加一个0
                }else{
                    totalSecond=second+"";
                }
                //歌曲当前播放时长
                minute = currentPosition / 1000 / 60;
                second = currentPosition / 1000 % 60;
                if(minute<10){//如果歌曲的时间中的分钟小于10
                    currMinute = "0" + minute;//在分钟的前面加一个0
                }else{
                    currMinute = minute+" ";
                }
                if (second<10){//如果歌曲中的秒钟小于10
                    currSecond = "0" + second;//在秒钟前面加一个0
                }else{
                    currSecond = second+" ";
                }
                time.setText(currMinute + ":" + currSecond + "/" + totalMinute + ":" + totalSecond);
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
        if (!permissionToRecordAccepted )
//            finish();
            display.setText("前往设置给予权限才可以使用录音识别功能");
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.playOrPause:
                //是否点击了录音按钮
                if(!Constant.isHaveRecordSource){
                    display.setText("请先点击录音按钮后再点击播放按钮");
                }else{
                    //是否是第一次点击播放
                    if(Constant.isFirstClickPlayer){
                        recordControl.play();
                    }else{
                        if (recordControl.isPlay()){
                            playOrPause.setImageResource(R.mipmap.play);
                            recordControl.pausePlay();
                        }else{
                            playOrPause.setImageResource(R.mipmap.pause);
                            recordControl.continuePlay();
                        }
                    }
                }
                break;
            case R.id.record:
                onRecord(mStartRecording);
                if (mStartRecording) {
                    record.setText("停止录音");
                } else {
                    record.setText("点击开始录音");
                }
                mStartRecording = !mStartRecording;
                break;
        }
    }


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }
    //开始录音
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(Constant.fileName);
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
        Constant.isHaveRecordSource = true;
        Constant.isFirstClickPlayer = true;
        display.setText("请稍等,正在识别");
        //开始语音识别
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

    }
}