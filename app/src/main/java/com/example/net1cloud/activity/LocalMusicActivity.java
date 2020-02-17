package com.example.net1cloud.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.net1cloud.R;
import com.example.net1cloud.adapter.LocalMusicRecycleViewAdapter;
import com.example.net1cloud.data.FragmentMsg;
import com.example.net1cloud.data.Music;
import com.example.net1cloud.fragment.LocalMusicChooseFragment;
import com.example.net1cloud.fragment.LocalMusicManageChooseFragment;
import com.example.net1cloud.fragment.ManageLocalMusicListFragment;
import com.example.net1cloud.utils.DeleteSongsUtil;
import com.example.net1cloud.utils.MusicInfoUtil;
import com.example.net1cloud.utils.PermissionUtil;
import com.hz.android.keyboardlayout.KeyboardLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    boolean isBundleNull;
    boolean isManaging = false;

    private LocalMusicRecycleViewAdapter localMusicRecycleViewAdapter;
    private LocalMusicActivityBroadcastReceiver localMusicActivityBroadcastReceiver;
    private RecyclerView localMusicListRecyclerView;
    private Toolbar toolbar;
    private SearchView searchView;
    private KeyboardLayout keyboardLayout;

    private FragmentManager fragmentManager;
   // private FragmentTransaction fragmentTransaction;
    private ManageLocalMusicListFragment manageLocalMusicListFragment;
    private LocalMusicManageChooseFragment localMusicManageChooseFragment;
    private LocalMusicChooseFragment localMusicChooseFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);

        isBundleNull = savedInstanceState == null;
        initEvent();
        initView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetFragmentChangeMessage(FragmentMsg fragmentMsg) {
        if ("LocalMusicManageChooseFragment".equals(fragmentMsg.getWhatFragment())) {
            if (fragmentMsg.getMsgString().equals(getString(R.string.manage))) {
                showBottomDialog(R.layout.local_music_manage_dialog);
                isManaging = true;
            } else if (fragmentMsg.getMsgString().equals(getString(R.string.playAll))) {
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
            }
        } else if ("LocalMusicChooseFragment".equals(fragmentMsg.getWhatFragment())) {
            if (fragmentMsg.getMsgString().equals(getString(R.string.complete))) {
                isManaging = false;
                localMusicRecycleViewAdapter.setChooseBoxVisibility(false);
                localMusicRecycleViewAdapter.notifyDataSetChanged();
                localMusicChooseFragment.setSelectAll(false);
                fragmentManager.beginTransaction().replace(R.id.manage_bar_container, localMusicManageChooseFragment).commit();
                fragmentManager.beginTransaction().hide(manageLocalMusicListFragment).commit();
            } else if (fragmentMsg.getMsgString().equals(getString(R.string.chooseAll))) {
                localMusicRecycleViewAdapter.setSelectAll();
                localMusicRecycleViewAdapter.notifyDataSetChanged();
            }
        } else if ("ManageLocalMusicListFragment".equals(fragmentMsg.getWhatFragment())) {
            if(localMusicRecycleViewAdapter.isSelectSongs()) {
                showBottomDialog(R.layout.confirm_delete_dialog);
            }
        }
    }

    private void initEvent() {
        registerActivityBroadcastReceiver();
        loadLocalMusic();
        initFragment();
        EventBus.getDefault().register(this);
    }

    private void initFragment() {
        fragmentManager = getSupportFragmentManager();
        manageLocalMusicListFragment = new ManageLocalMusicListFragment();
        localMusicManageChooseFragment = new LocalMusicManageChooseFragment();
        localMusicChooseFragment = new LocalMusicChooseFragment();
        fragmentManager.beginTransaction().replace(R.id.manage_bar_container, localMusicManageChooseFragment)
                .add(R.id.manage_local_music_list_fragment_container, manageLocalMusicListFragment)
                .hide(manageLocalMusicListFragment).commit();
    }

    private void initView() {
        localMusicListRecyclerView = findViewById(R.id.local_music_list_recycler_view);
        localMusicInfoList = MusicInfoUtil.getMusics(localMusicPathList);
        localMusicRecycleViewAdapter = new LocalMusicRecycleViewAdapter(localMusicInfoList, this);
        localMusicListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        localMusicListRecyclerView.setAdapter(localMusicRecycleViewAdapter);
        localMusicRecycleViewAdapter.setOnItemClickListener(position -> {
            if(!isManaging) {
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
                startActivityForResult(intent, REQUEST_CODE_MUSIC_PLAY);
            }
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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
                if(isManaging) {
                    isManaging = false;
                    localMusicRecycleViewAdapter.setChooseBoxVisibility(false);
                    localMusicRecycleViewAdapter.notifyDataSetChanged();
                    localMusicChooseFragment.setSelectAll(false);
                    fragmentManager.beginTransaction().replace(R.id.manage_bar_container, localMusicManageChooseFragment).commit();
                    fragmentManager.beginTransaction().hide(manageLocalMusicListFragment).commit();
                }
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
//            playAllRelativeLayout.setVisibility(View.VISIBLE);
            fragmentManager.beginTransaction().show(localMusicManageChooseFragment).commit();
            localMusicRecycleViewAdapter.setLocalMusicInfoList(localMusicInfoList);
            localMusicRecycleViewAdapter.notifyDataSetChanged();
        } else {
            what = what.toLowerCase();
//            playAllRelativeLayout.setVisibility(View.GONE);
            fragmentManager.beginTransaction().hide(localMusicManageChooseFragment).commit();
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
        EventBus.getDefault().unregister(this);
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
                i = (int) (Math.random() * localMusicPathList.size());
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
            if (resultCode == RESULT_FIRST_USER) {
                playPattern = getPlayPattern();
                if (data != null) {
                    index = data.getIntExtra("index", -1);
                    state = data.getIntExtra("state", -1);
                }
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
            for(File musicFile : Objects.requireNonNull(musicFiles.listFiles())) {
                if(!musicFile.getName().substring(musicFile.getName().lastIndexOf(".") + 1).equals("mp3"))
                    continue;
                localMusicPathList.add(musicFile.getAbsolutePath());
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

    private void showBottomDialog(int resource) {
        final Dialog dialog = new Dialog(this, R.style.DialogTheme);
        View view = View.inflate(this, resource, null);
        dialog.setContentView(view);

        Window window = dialog.getWindow();
        if (window != null) {
            //设置弹出位置
            window.setGravity(Gravity.BOTTOM);
            //设置弹出动画
            window.setWindowAnimations(R.style.main_menu_animStyle);
            //设置对话框大小
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.show();

        if(resource == R.layout.local_music_manage_dialog) {
            dialog.findViewById(R.id.local_music_multi_choose_dialog).setOnClickListener(view1 -> {
                dialog.dismiss();
                fragmentManager.beginTransaction().replace(R.id.manage_bar_container, localMusicChooseFragment)
                        .show(manageLocalMusicListFragment).commit();
                localMusicRecycleViewAdapter.setChooseBoxVisibility(true);
                localMusicRecycleViewAdapter.notifyDataSetChanged();
            });

            dialog.findViewById(R.id.local_music_sort_pattern_dialog).setOnClickListener(view12 -> dialog.dismiss());
        }

        if(resource == R.layout.confirm_delete_dialog) {
            dialog.findViewById(R.id.delete_local_music).setOnClickListener(view1 -> {
                dialog.dismiss();
                try {
                    for(int index : localMusicRecycleViewAdapter.getSelectedSongsIndex()) {
                        File song = new File(localMusicPathList.get(index));
                        DeleteSongsUtil.deleteSongs(song);
                        localMusicPathList.remove(index);
                        localMusicInfoList.remove(index);
                    }
                    localMusicRecycleViewAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(LocalMusicActivity.this, "删除失败\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            });

            dialog.findViewById(R.id.cancel_delete_local_music).setOnClickListener(view12 -> dialog.dismiss());
        }
    }
}
