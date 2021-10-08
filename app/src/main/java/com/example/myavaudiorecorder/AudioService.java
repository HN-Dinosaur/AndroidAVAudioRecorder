package com.example.myavaudiorecorder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.example.myavaudiorecorder.Model.Constant;

import java.util.Timer;
import java.util.TimerTask;

public class AudioService extends Service {
    private static final String LOG_TAG = "AudioRecordTest";
    private MediaPlayer player;
    private Timer timer;

    public AudioService() {}
    @Override
    public  IBinder onBind(Intent intent){
        Log.v(LOG_TAG,"onBind");
        return new RecordControl();
    }
    @Override
    public void onCreate(){
        super.onCreate();
        player = new MediaPlayer();//创建音乐播放器对象
    }

    public void addTimer(){ //添加计时器用于设置音乐播放器中的播放进度条
        if(timer == null){
            timer = new Timer();//创建计时器对象
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (player == null) return;
                    int duration = player.getDuration();//获取歌曲总时长
                    int currPosition = player.getCurrentPosition();//获取播放进度
                    Message msg = MainActivity.handler.obtainMessage();//创建消息对象
                    //将音乐的总时长和播放进度封装至消息对象中
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration",duration);
                    bundle.putInt("currentPosition",currPosition);
                    msg.setData(bundle);
                    //将消息发送到主线程的消息队列
                    MainActivity.handler.sendMessage(msg);
                }
            };
            //开始计时任务后的5毫秒，第一次执行task任务，以后每500毫秒执行一次
            timer.schedule(task,5,500);
        }
    }
    class RecordControl extends Binder{//Binder是一种跨进程的通信方式
        public void play(){
            try{
                Constant.isFirstClickPlayer = false;
                player.reset();//重置音乐播放器
                player.setDataSource(Constant.fileName);
                player.prepare();
                player.start();
                addTimer();//添加计时器
            }catch(Exception e){
                Log.e(LOG_TAG, "prepare() failed");
            }
        }
        public void pausePlay(){
            player.pause();//暂停播放音乐
        }
        public void continuePlay(){
            player.start();//继续播放音乐
        }
        public void seekTo(int progress){
            player.seekTo(progress);//设置音乐的播放位置
        }
        public boolean isPlay(){ return player.isPlaying(); }
    }

    @Override
    public void onDestroy(){
        if(player != null){
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}