package com.example.net1cloud.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.example.net1cloud.utils.MusicInfoUtil;

import org.jetbrains.annotations.NotNull;

public class PlayMusicService extends Service {

    public static final int START = 11;
    public static final int PAUSE = 12;
    public static final int CONTINUE = 13;
    public static final int ORDERLY = 0;
    public static final int RANDOMLY = 1;
    public static final int ONLY = 2;

    private int state = START;
    private int duration;
    private int currentPosition;
    private String music;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    public void onCreate() {
        super.onCreate();
        //注册广播
        registerServiceBroadcastReceiver();
        musicIsCompletionListener();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //监听歌曲播放是否完成
    public void musicIsCompletionListener() {
        //当歌曲播放完成后调用该方法
        mediaPlayer.setOnCompletionListener(mp -> {
            //发送广播给Activity播放下一曲
            Intent intent = new Intent("com.example.net1cloud.LocalMusicActivity");//列表页
            intent.putExtra("playFinish", true);
            sendBroadcast(intent);

            Intent intent2 = new Intent("com.example.net1cloud.PlayingActivity");//播放页
            intent2.putExtra("playFinish", true);
            sendBroadcast(intent2);

//                Intent intent3 = new Intent("com.example.net1cloud.MainActivity");//首页
//                intent3.putExtra("playFinish", 1);
//                sendBroadcast(intent3);

            currentPosition = 0;
        });
    }

    //注册广播接收器，用于接收activity发的广播
    private void registerServiceBroadcastReceiver() {
        PlayMusicServiceBroadcastReceiver playMusicServiceBroadcastReceiver = new PlayMusicServiceBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.net1cloud.playMusicService");
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);//拔出耳机的广播
        registerReceiver(playMusicServiceBroadcastReceiver, intentFilter);
    }

    class PlayMusicServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentMusicPlay = new Intent();

            //接收是否播放新歌曲
            boolean newMusic = intent.getBooleanExtra("newMusic", false);
            if (newMusic) {
                music = intent.getStringExtra("musicPath");
                if (music != null && !music.equals("")) {
                    //播放歌曲
                    playMusic(music);
                    state = PAUSE;
                }
            }

            //如果是暂停播放按钮，根据state控制播放状态
            boolean isPlayOrPause = intent.getBooleanExtra("isPlayOrPause", false);
            if (isPlayOrPause) {
                switch (state) {
                    //第一次播放歌曲
                    case START:
                        music = intent.getStringExtra("musicPath");
                        playMusic(music);
                        state = PAUSE;
                        break;
                    //暂停
                    case PAUSE:
                        mediaPlayer.pause();
                        state = CONTINUE;
                        break;
                    //继续播放
                    case CONTINUE:
                        mediaPlayer.start();
                        state = PAUSE;
                        break;
                }
                intentMusicPlay.putExtra("isPlayOrPause", true);
            }

            //拖动进度条发送的广播，先获取歌曲进度位置
            int progress = intent.getIntExtra("progress", -1);
            if (progress != -1) {
                //转换为播放歌曲的时间(毫秒)
                progress = (progress * mediaPlayer.getDuration() / 1000);
                mediaPlayer.seekTo(progress);
            }

            //拔出耳机，暂停
            if(intent.getAction() != null && intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
                mediaPlayer.pause();
                state = START;
            }

//            //将当前状态发送给Activity更新按钮
//            Intent intent2 = new Intent();
//            intent2.setAction("com.example.net1cloud.LocalMusicActivity");//列表
//            intent2.putExtra("state", state);
//            sendBroadcast(intent2);


            intentMusicPlay.setAction("com.example.net1cloud.PlayingActivity");//播放页
            intentMusicPlay.putExtra("state", state);
            sendBroadcast(intentMusicPlay);

//            Intent intentHome = new Intent();
//            intentHome.setAction("com.example.net1cloud.MainActivity");//首页
//            intentHome.putExtra("state", state);
//            sendBroadcast(intentHome);
        }
    }

    //播放歌曲
    public void playMusic(String musicPath) {
        //player不为空，说明正在播放歌曲
        if (mediaPlayer != null) {
            //停止播放
            mediaPlayer.stop();
            //等待
            mediaPlayer.reset();
            try {
                //获取歌曲播放路径
                mediaPlayer.setDataSource(musicPath);
                //准备歌曲
                mediaPlayer.prepare();
                //播放歌曲
                mediaPlayer.start();
                MusicInfoUtil.NowMusicPath = musicPath;
                duration = mediaPlayer.getDuration();//获取当前播放歌曲总时长

                new updateProgressThread().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class updateProgressThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1;
                    mHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (currentPosition < duration);
        }
    }

    //在主线程里面处理消息并更新UI界面
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NotNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                currentPosition = mediaPlayer.getCurrentPosition();//获取播放歌曲的当前时间
//                Intent intent = new Intent("com.example.net1cloud.LocalMusicActivity");//列表页
//                intent.putExtra("currentPosition", currentPosition);
//                intent.putExtra("duration", duration);
//                intent.putExtra("state", state);
//                sendBroadcast(intent);

                Intent intent2 = new Intent("com.example.net1cloud.PlayingActivity");//播放页
                intent2.putExtra("currentPosition", currentPosition);
                intent2.putExtra("duration", duration);
                intent2.putExtra("state", state);
                sendBroadcast(intent2);

//                Intent intent3 = new Intent("com.example.net1cloud.MainActivity");//首页
//                intent3.putExtra("currentPosition", currentPosition);
//                intent3.putExtra("duration", duration);
//                intent3.putExtra("state", state);
//                sendBroadcast(intent3);
            }
        }
    };

}
