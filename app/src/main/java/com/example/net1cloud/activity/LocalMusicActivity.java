package com.example.net1cloud.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.net1cloud.R;
import com.example.net1cloud.adapter.LocalMusicRecycleViewAdapter;
import com.example.net1cloud.utils.MusicInfoUtil;
import com.example.net1cloud.utils.PermissionUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;
import static com.example.net1cloud.service.PlayMusicService.ONLY;
import static com.example.net1cloud.service.PlayMusicService.ORDERLY;
import static com.example.net1cloud.service.PlayMusicService.RANDOMLY;
import static com.example.net1cloud.service.PlayMusicService.START;

public class LocalMusicActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MUSIC_PLAY = 1;

    private List<String> localMusicPathList = new ArrayList<>();
    //private List<Music> musicList = new ArrayList<>();
    private int index = 0;
    private int state = START;
    private int playPattern = ORDERLY;//0：列表循环 1：随机播放 2：单曲循环
    //private Music music;

    private RecyclerView localMusicListRecyclerView;
    private LocalMusicRecycleViewAdapter localMusicRecycleViewAdapter;
    private LocalMusicActivityBroadcastReceiver localMusicActivityBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);

        registerActivityBroadcastReceiver();

        initEvent();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(localMusicActivityBroadcastReceiver);
    }

    private void registerActivityBroadcastReceiver() {
        localMusicActivityBroadcastReceiver = new LocalMusicActivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.net1cloud.LocalMusicActivity");
        registerReceiver(localMusicActivityBroadcastReceiver, intentFilter);
    }

    class LocalMusicActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //歌曲是否播放完，播放完则播放下一曲
            boolean playFinish = intent.getBooleanExtra("playFinish", false);
            if (playFinish) {
                index = getNextIndex();
                //music = musicList.get(index);
                //showPlayInfo();
                //发送广播通知服务播放新歌曲
                Intent intent2 = new Intent();
                intent2.setAction("com.example.net1cloud.playMusicService");
                intent2.putExtra("musicPath", localMusicPathList.get(index));
                intent2.putExtra("newMusic", true);
                sendBroadcast(intent2);
            }

            //获取播放状态更改UI（暂停，播放）,state:10为播放第一首歌曲 11为暂停 12为继续播放
                state = intent.getIntExtra("state", -1);
//            if (state != -1) {
//                updatePlayOrPauseUI(state);
//            }

//            int currPosition = intent.getIntExtra("currPosition", -1);
//            int duration = intent.getIntExtra("duration", -1);
//            if (currPosition != -1) {
//                //将当前歌曲时间转化为位置
//                int progress = (int) ((currPosition * 1.0) / duration * 100);
//                progressBar.setProgress(progress);
//            }
        }
    }

    //下一首歌索引
    public int getNextIndex() {
        int i = 0;
        if (playPattern == ORDERLY) {//顺序播放
            if (index != localMusicPathList.size() - 1)
                i = index++;
        }
        if (playPattern == RANDOMLY) {//随机播放
            do {
                i = (int) (Math.random() * (localMusicPathList.size() - 1));
            } while (i == index);
        }
        if (playPattern == ONLY) {//单曲循环
            i = index;
        }
        return i;
    }

    private void initEvent() {
        loadLocalMusic();
    }

    private void initView() {
        localMusicListRecyclerView = findViewById(R.id.local_music_list_recycler_view);
        localMusicRecycleViewAdapter = new LocalMusicRecycleViewAdapter(localMusicPathList);
        localMusicListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        localMusicListRecyclerView.setAdapter(localMusicRecycleViewAdapter);
        localMusicRecycleViewAdapter.setOnItemClickListener(position -> {
            //发送广播通知服务播放新歌曲
            index = position;

            if(!MusicInfoUtil.NowMusicPath.equals(localMusicPathList.get(position))){
                Intent intent1 = new Intent();
                intent1.setAction("com.example.net1cloud.playMusicService");
                intent1.putExtra("musicPath", localMusicPathList.get(position));
                intent1.putExtra("newMusic", true);
                sendBroadcast(intent1);
            }

            Intent intent = new Intent(LocalMusicActivity.this, PlayingActivity.class);
            intent.putExtra("localMusicPathList", (Serializable) localMusicPathList);
            intent.putExtra("index", index);
            intent.putExtra("state", state);
            startActivity(intent);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_MUSIC_PLAY:
                if (resultCode == RESULT_OK) {
                    playPattern = getPlayPattern();
                    index = data.getIntExtra("index", -1);
                    state = data.getIntExtra("state", -1);
                    if (index != -1) {
                        //刷新播放信息ui
                        //showPlayInfo();
                        //TODO://////////////
                        //listView.setSelection(index);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void loadLocalMusic() {
        PermissionUtil.verifyStoragePermissions(LocalMusicActivity.this);
        try {
            String SDCardPath = getExternalStorageDirectory().getAbsolutePath();
            File musicFiles = new File(SDCardPath, "/Music");
            for(int i = 0; i < musicFiles.listFiles().length; i++) {
                localMusicPathList.add(musicFiles.listFiles()[i].getAbsolutePath());
            }
        } catch (Exception e) {
            Toast.makeText(LocalMusicActivity.this, "加载本地音乐失败\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private int getPlayPattern() {
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        int pattern = sharedPreferences.getInt("playPattern", -1);
        return pattern == -1 ? 0 : pattern;
    }
}
