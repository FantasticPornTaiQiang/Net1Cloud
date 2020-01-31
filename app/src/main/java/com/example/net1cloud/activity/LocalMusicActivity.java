package com.example.net1cloud.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.net1cloud.R;
import com.example.net1cloud.adapter.LocalMusicRecycleViewAdapter;
import com.example.net1cloud.data.Music;
import com.example.net1cloud.utils.MusicInfoUtil;
import com.example.net1cloud.utils.PermissionUtil;
import com.hz.android.keyboardlayout.KeyboardLayout;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.os.Environment.getExternalStorageDirectory;
import static com.example.net1cloud.service.PlayMusicService.ONLY;
import static com.example.net1cloud.service.PlayMusicService.ORDERLY;
import static com.example.net1cloud.service.PlayMusicService.RANDOMLY;
import static com.example.net1cloud.service.PlayMusicService.START;

public class LocalMusicActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_MUSIC_PLAY = 1;

    private List<String> localMusicPathList = new ArrayList<>();
    private List<Music> localMusicResultList = new ArrayList<>();
    private List<Music> localMusicInfoList = new ArrayList<>();
    private int index = 0;
    private int state = START;
    private int playPattern = ORDERLY;//0：列表循环 1：随机播放 2：单曲循环
    private boolean isSearchResult = false;

    private LocalMusicRecycleViewAdapter localMusicRecycleViewAdapter;
    private LocalMusicActivityBroadcastReceiver localMusicActivityBroadcastReceiver;
    private RecyclerView localMusicListRecyclerView;
    private Toolbar toolbar;
    private SearchView searchView;
    private RelativeLayout playAllRelativeLayout;
    private ImageView manageLocalMusicListButton;
    private KeyboardLayout keyboardLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);

        registerActivityBroadcastReceiver();

        initEvent();
        initView();
    }

    private void initEvent() {
        loadLocalMusic();
    }

    //TODO:1.管理歌单操作（记得同步本地、localMusicPathList、localMusicInfoList）

    private void initView() {
        localMusicListRecyclerView = findViewById(R.id.local_music_list_recycler_view);
        localMusicInfoList = MusicInfoUtil.getMusics(localMusicPathList);
        localMusicRecycleViewAdapter = new LocalMusicRecycleViewAdapter(localMusicInfoList);
        localMusicListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        localMusicListRecyclerView.setAdapter(localMusicRecycleViewAdapter);
        localMusicRecycleViewAdapter.setOnItemClickListener(position -> {
            if(!isSearchResult) {
                index = position;
            } else {
                Music music = localMusicResultList.get(position);
                int i = 0;
                for(; i < localMusicInfoList.size(); i++){
                    if(localMusicInfoList.get(i).getName().equals(music.getName()) &&
                            localMusicInfoList.get(i).getAlbum().equals(music.getAlbum()) &&
                            localMusicInfoList.get(i).getArtist().equals(music.getArtist())) {
                        break;
                    }
                }
                index = i;
            }

            //发送广播通知服务播放新歌曲
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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        playAllRelativeLayout = findViewById(R.id.layout_play_all);
        playAllRelativeLayout.setOnClickListener(view -> {
            index = 0;
            //点击全部播放则默认播放第一首歌
            if(!MusicInfoUtil.NowMusicPath.equals(localMusicPathList.get(index))){
                Intent intent1 = new Intent();
                intent1.setAction("com.example.net1cloud.playMusicService");
                intent1.putExtra("musicPath", localMusicPathList.get(index));
                intent1.putExtra("newMusic", true);
                sendBroadcast(intent1);
            }

            Intent intent = new Intent(LocalMusicActivity.this, PlayingActivity.class);
            intent.putExtra("localMusicPathList", (Serializable) localMusicPathList);
            intent.putExtra("index", index);
            intent.putExtra("state", state);
            startActivity(intent);
        });

        manageLocalMusicListButton = findViewById(R.id.manage_local_music_list_btn);
        manageLocalMusicListButton.setOnClickListener(view -> {
            //TODO::
        });

        keyboardLayout = findViewById(R.id.keyboard_layout);
        keyboardLayout.setKeyboardLayoutListener((isActive, keyboardHeight) -> {
            if(!isActive) {
                if(searchView != null)
                searchView.clearFocus();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.searchview_as_a_menu_in_actionbar, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //输入完成后，点击回车或是完成键
            @Override
            public boolean onQueryTextSubmit(final String query) {
                searchView.clearFocus();
                return true;
            }
            //查询文本框有变化时事件
            @Override
            public boolean onQueryTextChange(final String newText) {
                isSearchResult = !newText.trim().equals("");
                updateSearchResult(newText);
                return true;
            }
        });
        searchView.setQueryHint("搜索本地歌曲");
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);


        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });

        menu.findItem(R.id.menu_search).expandActionView();
        searchView.clearFocus();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSearchResult(String what) {
        //不写这个if也是可以的，太巧妙了！
        if(what.trim().equals("")){
            playAllRelativeLayout.setVisibility(View.VISIBLE);
            localMusicRecycleViewAdapter.setLocalMusicInfoList(localMusicInfoList);
            localMusicRecycleViewAdapter.notifyDataSetChanged();
        } else {
            what = what.toLowerCase();
            playAllRelativeLayout.setVisibility(View.GONE);
            localMusicResultList.clear();
            for(Music localMusicInfo : localMusicInfoList) {
                if(localMusicInfo.getName().toLowerCase().contains(what)
                        || localMusicInfo.getArtist().toLowerCase().contains(what)
                        || localMusicInfo.getAlbum().toLowerCase().contains(what)) {
                    localMusicResultList.add(localMusicInfo);
                }
            }
            localMusicRecycleViewAdapter.setLocalMusicInfoList(localMusicResultList);
            localMusicRecycleViewAdapter.notifyDataSetChanged();
        }
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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MUSIC_PLAY) {
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
        }
    }

    private void loadLocalMusic() {
        PermissionUtil.verifyStoragePermissions(LocalMusicActivity.this);
        try {
            File musicFiles = new File(getExternalStorageDirectory().getAbsolutePath(), "/Music");
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
