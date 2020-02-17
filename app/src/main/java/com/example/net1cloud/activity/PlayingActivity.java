package com.example.net1cloud.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.net1cloud.R;
import com.example.net1cloud.adapter.PlayingMoreRecyclerViewAdapter;
import com.example.net1cloud.data.FragmentMsg;
import com.example.net1cloud.data.Music;
import com.example.net1cloud.fragment.SaveAlbumImageFragment;
import com.example.net1cloud.utils.LyricUtil;
import com.example.net1cloud.utils.MusicInfoUtil;
import com.example.net1cloud.utils.TimeUtil;
import com.example.net1cloud.utils.ToastUtil;
import com.example.net1cloud.widget.LyricView;
import com.example.net1cloud.widget.PlaySeekBar;
import com.example.net1cloud.widget.RoundAlbumAndNeedle;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import static com.example.net1cloud.service.PlayMusicService.CONTINUE;
import static com.example.net1cloud.service.PlayMusicService.ONLY;
import static com.example.net1cloud.service.PlayMusicService.ORDERLY;
import static com.example.net1cloud.service.PlayMusicService.PAUSE;
import static com.example.net1cloud.service.PlayMusicService.RANDOMLY;
import static com.example.net1cloud.service.PlayMusicService.START;

public class PlayingActivity extends AppCompatActivity{

    //TODO:1.bug：在歌词界面但已经播放下一首歌曲，以及暂停状态下从歌词回到albumView的RoundAlbumAndNeedle控件的动画问题
    //TODO:2.切换歌曲的歌词加载，记得使用等待提示与reset，并增加长按复制该行歌词功能
    //TODO:3.歌单的GreenDao数据库与播放列表换源
    //TODO:4.定时关闭
    //TODO:5.音量控制
    //TODO:6.歌单管理界面的顺序调整
    //TODO:7.用ViewPager切换歌曲
    //TODO:8.歌单：本地、播放列表、我喜欢的音乐、歌曲来源、创建的歌单
    //TODO:9.本地歌单按专辑或歌手检索歌曲
    //TODO:10.bug：当mp3文件缺少音乐信息时的默认显示处理
    //TODO:*11.查明加载缓慢的原因并修复
    //TODO:12.各个歌单的每个歌曲itemView的小菜单选项
    //TODO:*13.屏幕适配以及文件存储路径优化

    private ActionBar actionBar;
    private Toolbar toolbar;
    private PlaySeekBar playSeekBar;
    private TextView currentTextView;
    private TextView totalTextView;
    private ImageView playingPlayButton;
    private ImageView playingModeButton;
    private ImageView playingPreButton;
    private ImageView playingNextButton;
    private RoundAlbumAndNeedle roundAlbumAndNeedle;
    private ImageView playingMoreButton;
//    private AlbumViewPager albumViewPager;
//    private FragmentAdapter albumAdapter;
    private RelativeLayout albumViewContainer;
    private RelativeLayout lyricViewContainer;
    private LyricView lyricView;

    private FragmentManager fragmentManager;
    private SaveAlbumImageFragment saveAlbumImageFragment;

    private List<Music> musicList = new ArrayList<>();
    private int index = 0;
    private int duration;
    private boolean isTrack = false;
    private boolean isLrcSeekByUser = false;
    private int playPattern;//0：列表循环 1：随机播放 2：单曲循环
    private int state = START;//11为播放第一首歌曲 12为暂停 13为继续播放
    private String source = "本地音乐";

//    private static final int VIEWPAGER_SCROLL_TIME = 390;

    private MusicPlayActivityBroadcastReceiver musicPlayActivityBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);

        boolean isBundleNull = savedInstanceState == null;

        initData();
        initView(isBundleNull);
        initEvent();
    }

    @Override
    public void onBackPressed() {
        //stopAnim();
        Intent intent = new Intent();
        intent.putExtra("index", index);
        intent.putExtra("state", state);
        setResult(RESULT_FIRST_USER, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(musicPlayActivityBroadcastReceiver);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetFragmentChangeMessage(FragmentMsg fragmentMsg) {
        if ("SaveAlbumImageFragment".equals(fragmentMsg.getWhatFragment())) {
            if (fragmentMsg.getMsgString().equals(getString(R.string.hideFragment))) {
                fragmentManager.beginTransaction().hide(saveAlbumImageFragment).commit();
            }
        }
    }

    /**
     * 注册广播接收器，用于接收serivce发的广播
     */
    private void registerActivityBroadcastReceiver() {
        musicPlayActivityBroadcastReceiver = new MusicPlayActivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.net1cloud.PlayingActivity");
        registerReceiver(musicPlayActivityBroadcastReceiver, intentFilter);
    }

    class MusicPlayActivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //歌曲是否播放完，播放完则播放下一曲
            boolean playFinish = intent.getBooleanExtra("playFinish", false);
            if (playFinish) {
                index = getNextIndex(true);
                showPlayInfo();
                //发送广播通知服务播放新歌曲
                Intent intent2 = new Intent();
                intent2.setAction("com.example.net1cloud.playMusicService");
                intent2.putExtra("musicPath", musicList.get(index).getMusicPath());
                intent2.putExtra("newMusic", true);
                sendBroadcast(intent2);

                if(!musicList.get(index).isHasAlbumImage()) {
                    musicList.get(index).setAlbumImage(((BitmapDrawable) getResources().getDrawable(R.drawable.placeholder_disk_play_song, null)).getBitmap());
                    musicList.get(index).setHasAlbumImage(true);
                }
                roundAlbumAndNeedle.next(musicList.get(index).getAlbumImage());
            }

            //获取播放状态更改UI（暂停，播放）,state:10为播放第一首歌曲 11为暂停 12为继续播放
            state = intent.getIntExtra("state", -1);
            boolean isPlayOrPause = intent.getBooleanExtra("isPlayOrPause", false);
            if (state != -1) {
                updatePlayOrPauseUI(state, !isPlayOrPause);
                //musicPlayBg(state);
            }

            int currentPosition = intent.getIntExtra("currentPosition", -1);
            duration = intent.getIntExtra("duration", -1);
            if (currentPosition != -1) {
                //将当前歌曲时间转化为位置
                if(!isTrack){
                    int progress = ((currentPosition * 1000) / duration);
                    playSeekBar.setProgress(progress);
                    lyricView.setProgress(currentPosition / 1000, isLrcSeekByUser);
                    if(isLrcSeekByUser) isLrcSeekByUser = false;
                    currentTextView.setText(TimeUtil.getTimeStrFromMilliSeconds(currentPosition));
                }
            }

        }
    }

    //上一首歌索引
    public int getPreIndex() {
        int i = 0;
        if (playPattern == ORDERLY || playPattern == ONLY) {//顺序播放、单曲循环
            if (index == 0) {
                i = musicList.size() - 1;
            } else {
                i = --index;
            }
        }
        if (playPattern == RANDOMLY) {//随机播放
            do {
                i = (int) (Math.random() * musicList.size());
            } while (i == index);
        }
        return i;
    }

    //下一首歌索引
    public int getNextIndex(boolean isAutoNext) {
        int i = 0;
        if (playPattern == ORDERLY || (playPattern == ONLY && !isAutoNext)) {//顺序播放
            if (index != musicList.size() - 1)
                i = ++index;
        }
        if (playPattern == RANDOMLY) {//随机播放
            do {
                i = (int) (Math.random() * musicList.size());
            } while (i == index);
        }
        if (playPattern == ONLY && isAutoNext) {//单曲循环
            i = index;
        }
        return i;
    }

    //从SharedPreference获取playPattern
    private int getPlayPattern() {
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        int pattern = sharedPreferences.getInt("playPattern", -1);
        return pattern == -1 ? 0 : pattern;
    }

    //播放模式改变
    @SuppressLint("ShowToast")
    public void playPatternChange(boolean toast) {
        if (playPattern > 2) {
            playPattern = 0;
        }
        //将播放方式存至本地
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putInt("playPattern", playPattern);
        editor.apply();

        switch (playPattern) {
            case ORDERLY:
                if(toast)
                    ToastUtil.showMyToast(Toast.makeText(this, "列表循环", Toast.LENGTH_SHORT), 1500);
                playingModeButton.setImageResource(R.drawable.play_icn_loop_prs);
                break;
            case RANDOMLY:
                if(toast)
                    ToastUtil.showMyToast(Toast.makeText(this, "随机播放", Toast.LENGTH_SHORT), 1500);
                playingModeButton.setImageResource(R.drawable.play_icn_shuffle);
                break;
            case ONLY:
                if(toast)
                    ToastUtil.showMyToast(Toast.makeText(this, "单曲循环", Toast.LENGTH_SHORT), 1500);
                playingModeButton.setImageResource(R.drawable.play_icn_one_prs);
                break;
            default:
                break;
        }
    }

    private void showPlayInfo() {
        actionBar.setTitle(musicList.get(index).getName());
        actionBar.setSubtitle(musicList.get(index).getArtist());
        totalTextView.setText(musicList.get(index).getDuration());
    }

    @SuppressWarnings("unchecked")
    private void initData() {
        Intent intent = getIntent();
        index = intent.getIntExtra("index", -1);
        state = intent.getIntExtra("state", -1);
        playPattern = getPlayPattern();
        List<String> localMusicPathList = (List<String>) intent.getSerializableExtra("localMusicPathList");

        if(localMusicPathList != null) {
            for(String localMusicPath : localMusicPathList){
                musicList.add(MusicInfoUtil.getMusic(localMusicPath));
            }
        }
    }

    private void initEvent() {
        registerActivityBroadcastReceiver();
        updatePlayOrPauseUI(state, false);
        showPlayInfo();
        EventBus.getDefault().register(this);
    }

    private void initView(boolean isBundleNull) {
        initFragment(isBundleNull);

        totalTextView = findViewById(R.id.music_duration);
        totalTextView.setText(musicList.get(index).getDuration());
        currentTextView = findViewById(R.id.music_duration_played);
        playingPlayButton = findViewById(R.id.playing_play);
        playingPlayButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction("com.example.net1cloud.playMusicService");
            intent.putExtra("musicPath", musicList.get(index).getMusicPath());
            intent.putExtra("isPlayOrPause", true);//判断是否是点击了播放/暂停（这个按钮才需判断播放状态）
            sendBroadcast(intent);
        });
        playSeekBar = findViewById(R.id.play_seek_bar);
        playSeekBar.setIndeterminate(false);//明确显示进度
        playSeekBar.setProgress(1);
        playSeekBar.setMax(1000);
        setSeekBarListener();
        toolbar = findViewById(R.id.playing_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            actionBar = getSupportActionBar();
            assert actionBar != null;
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.actionbar_back);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
        playingModeButton = findViewById(R.id.playing_mode);
        playPatternChange(false);
        playingModeButton.setOnClickListener(view -> {
            playPattern++;
            playPatternChange(true);
        });
        playingPreButton = findViewById(R.id.playing_pre);
        playingNextButton = findViewById(R.id.playing_next);
        playingPreButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            index = getPreIndex();
            showPlayInfo();
            intent.setAction("com.example.net1cloud.playMusicService");
            intent.putExtra("musicPath", musicList.get(index).getMusicPath());
            intent.putExtra("newMusic", true);
            sendBroadcast(intent);
            if(!musicList.get(index).isHasAlbumImage()) {
                musicList.get(index).setAlbumImage(((BitmapDrawable) getResources().getDrawable(R.drawable.placeholder_disk_play_song, null)).getBitmap());
                musicList.get(index).setHasAlbumImage(true);
            }
            roundAlbumAndNeedle.prev(musicList.get(index).getAlbumImage());
        });
        playingNextButton.setOnClickListener(view -> {
            Intent intent = new Intent();
            index = getNextIndex(false);
            showPlayInfo();
            intent.setAction("com.example.net1cloud.playMusicService");
            intent.putExtra("musicPath", musicList.get(index).getMusicPath());
            intent.putExtra("newMusic", true);
            sendBroadcast(intent);
            if(!musicList.get(index).isHasAlbumImage()) {
                musicList.get(index).setAlbumImage(((BitmapDrawable) getResources().getDrawable(R.drawable.placeholder_disk_play_song, null)).getBitmap());
                musicList.get(index).setHasAlbumImage(true);
            }
            roundAlbumAndNeedle.next(musicList.get(index).getAlbumImage());
        });
        roundAlbumAndNeedle = findViewById(R.id.round_album_and_needle_view);
        if(!musicList.get(index).isHasAlbumImage()) {
            musicList.get(index).setAlbumImage(((BitmapDrawable) getResources().getDrawable(R.drawable.placeholder_disk_play_song, null)).getBitmap());
            musicList.get(index).setHasAlbumImage(true);
        }
        roundAlbumAndNeedle.setAlbumImage(musicList.get(index).getAlbumImage());
//        albumViewPager = findViewById(R.id.album_view_pager);
//        setViewPager();

        roundAlbumAndNeedle.setOnLongClickListener(() -> {
            saveAlbumImageFragment.setAlbumImage(musicList.get(index).getAlbumImage());
            fragmentManager.beginTransaction().show(saveAlbumImageFragment).commit();
        });
        playingMoreButton = findViewById(R.id.playing_more);
        playingMoreButton.setOnClickListener(view -> showBottomDialog(R.layout.playing_more_dialog));
        albumViewContainer = findViewById(R.id.album_view_container);
        lyricViewContainer = findViewById(R.id.lyric_view_container);
        lyricViewContainer.setVisibility(View.INVISIBLE);
        albumViewContainer.setOnClickListener(view -> {
            lyricViewContainer.setVisibility(View.VISIBLE);
            albumViewContainer.setVisibility(View.INVISIBLE);
        });
        lyricView = findViewById(R.id.lyric_view);
        lyricView.setLrcRows(LyricUtil.parseToLyricRowList(this, musicList.get(index).getName(),
                musicList.get(index).getDurationInt()));
        lyricView.setOnPlayClickListener(progress -> {
            Intent intent = new Intent("com.example.net1cloud.playMusicService");
            intent.putExtra("progress", progress * 1000 / duration);
            if(state == CONTINUE)
                intent.putExtra("isPlayOrPause", true);//相当于click了一次播放按钮
            sendBroadcast(intent);
        });
        lyricView.setOnViewClickListener(() -> {
            albumViewContainer.setVisibility(View.VISIBLE);
            lyricViewContainer.setVisibility(View.INVISIBLE);
        });
    }

    private void initFragment(boolean isBundleNull) {
        fragmentManager = getSupportFragmentManager();
        saveAlbumImageFragment = new SaveAlbumImageFragment();
        if(isBundleNull)
            fragmentManager.beginTransaction().add(R.id.save_album_image_container, saveAlbumImageFragment).hide(saveAlbumImageFragment).commit();
    }

    //根据状态更改播放暂停按钮UI界面
    //播放有三种情况，1.刚进入此activity需要手动设置播放图标，相当于在代码中点了一下控件
    //2.播放完歌曲，进入下一首，更新图标，此时只更新图标，圆盘的处理逻辑已经内部写好，无需改动[bug在此产生]
    //3.歌曲已经是暂停状态，用户点击播放，更新图标
    //有个bug，state会有一次值为-1，暂时先采用笨办法解决
    private void updatePlayOrPauseUI(int state, boolean isNext) {
        switch (state) {
            case START:
            case PAUSE:
                if(!isNext)
                    roundAlbumAndNeedle.play();
                lyricView.play();
                playingPlayButton.setImageResource(R.drawable.play_rdi_btn_pause);
                break;
            case CONTINUE:
                roundAlbumAndNeedle.pause();
                lyricView.pause();
                playingPlayButton.setImageResource(R.drawable.play_rdi_btn_play);
                break;
            default:
                if(!isNext)
                    roundAlbumAndNeedle.play();
                lyricView.play();
                break;
        }
    }

    private void setSeekBarListener() {
        if (playSeekBar != null)
            playSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //拖动时更新UI，progress：进度(0~1000)，fromUser：是否由用户操作导致改变的
                    if(fromUser)
                        currentTextView.setText(TimeUtil.getTimeStrFromMilliSeconds((long)((progress / 1000.) * duration)));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //拖动开始，暂停对SeekBar的监听
                    isTrack = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //拖动停止,发送广播更新播放位置
                    isTrack = false;
                    isLrcSeekByUser = true;
                    int progress = seekBar.getProgress();
                    Intent intent = new Intent("com.example.net1cloud.playMusicService");
                    intent.putExtra("progress", progress);
                    sendBroadcast(intent);
                }
            });
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
//            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dm.heightPixels * 3 / 5);
        }
        dialog.show();

        if(resource == R.layout.playing_more_dialog) {
            RecyclerView playingMoreRecyclerView = dialog.findViewById(R.id.playing_more_recycler_view);
            playingMoreRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            PlayingMoreRecyclerViewAdapter playingMoreRecyclerViewAdapter =
                    new PlayingMoreRecyclerViewAdapter(musicList.get(index).getArtist(), musicList.get(index).getAlbum(), source, this);
            playingMoreRecyclerView.setAdapter(playingMoreRecyclerViewAdapter);

            ((ImageView)dialog.findViewById(R.id.album_image)).setImageBitmap(musicList.get(index).getAlbumImage());
            ((TextView)dialog.findViewById(R.id.song_name)).setText(musicList.get(index).getName());
            ((TextView)dialog.findViewById(R.id.singer_name)).setText(musicList.get(index).getArtist());

            playingMoreRecyclerViewAdapter.setOnItemClickListener(position -> {
                switch (position) {
                    case 0:
                        //TODO:收藏到歌单
                        break;
                    case 3:
                        //TODO:来源
                        break;
                    case 8:
                        //TODO:定时关闭
                        break;
                    default:
                        break;
                }
            });
        }


    }


    //    private void setViewPager() {
//        albumViewPager.setOffscreenPageLimit(2);
//        PlaybarPagerTransformer transformer = new PlaybarPagerTransformer();
//        albumAdapter = new FragmentAdapter(getSupportFragmentManager());
//        albumViewPager.setAdapter(albumAdapter);
//        albumViewPager.setPageTransformer(true, transformer);
//
//        // 改变viewpager动画时间
//        try {
//            Field mField = ViewPager.class.getDeclaredField("mScroller");
//            mField.setAccessible(true);
//            MyScroller mScroller = new MyScroller(albumViewPager.getContext().getApplicationContext(), new LinearInterpolator());
//            mField.set(albumViewPager, mScroller);
//        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//        albumViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//
//            @Override
//            public void onPageSelected(final int pPosition) {
//                if (pPosition < 1) { //首位之前，跳转到末尾（N）
//                    MusicPlayer.setQueuePosition(MusicPlayer.getQueue().length);
//                    albumViewPager.setCurrentItem(MusicPlayer.getQueue().length, false);
//                    isNextOrPreSetPage = false;
//                    return;
//
//                } else if (pPosition > MusicPlayer.getQueue().length) { //末位之后，跳转到首位（1）
//                    MusicPlayer.setQueuePosition(0);
//                    albumViewPager.setCurrentItem(1, false); //false:不显示跳转过程的动画
//                    isNextOrPreSetPage = false;
//                    return;
//                } else {
//
//                    if (!isNextOrPreSetPage) {
//                        if (pPosition < MusicPlayer.getQueuePosition() + 1) {
////                            HandlerUtil.getInstance(PlayingActivity.this).postDelayed(new Runnable() {
////                                @Override
////                                public void run() {
////                                  //  MusicPlayer.previous(PlayingActivity.this, true);
////                                    Message msg = new Message();
////                                    msg.what = 0;
////                                    mPlayHandler.sendMessage(msg);
////                                }
////                            }, 500);
//
//                            Message msg = new Message();
//                            msg.what = PRE_MUSIC;
//                            mPlayHandler.sendMessageDelayed(msg, TIME_DELAY);
//
//
//                        } else if (pPosition > MusicPlayer.getQueuePosition() + 1) {
////                            HandlerUtil.getInstance(PlayingActivity.this).postDelayed(new Runnable() {
////                                @Override
////                                public void run() {
////                                  //  MusicPlayer.mNext();
////
////
////                                }
////                            }, 500);
//
//                            Message msg = new Message();
//                            msg.what = NEXT_MUSIC;
//                            mPlayHandler.sendMessageDelayed(msg,TIME_DELAY);
//
//                        }
//                    }
//
//                }
//                //MusicPlayer.setQueuePosition(pPosition - 1);
//                isNextOrPreSetPage = false;
//
//            }
//
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int pState) {
//            }
//        });
//    }
//
//    public class MyScroller extends Scroller {
//        private int animTime = VIEWPAGER_SCROLL_TIME;
//
//        public MyScroller(Context context) {
//            super(context);
//        }
//
//        public MyScroller(Context context, Interpolator interpolator) {
//            super(context, interpolator);
//        }
//
//        @Override
//        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
//            super.startScroll(startX, startY, dx, dy, animTime);
//        }
//
//        @Override
//        public void startScroll(int startX, int startY, int dx, int dy) {
//            super.startScroll(startX, startY, dx, dy, animTime);
//        }
//
//        public void setmDuration(int animTime) {
//            this.animTime = animTime;
//        }
//    }
//
//    public class PlaybarPagerTransformer implements ViewPager.PageTransformer {
//
//        @Override
//        public void transformPage(View view, float position) {
//
//            if (position == 0) {
//                if (MusicPlayer.isPlaying()) {
//                    mRotateAnim = (ObjectAnimator) view.getTag(R.id.tag_animator);
//                    if (mRotateAnim != null && !mRotateAnim.isRunning() && mNeedleAnim != null) {
//                        mAnimatorSet = new AnimatorSet();
//                        mAnimatorSet.play(mNeedleAnim).before(mRotateAnim);
//                        mAnimatorSet.start();
//                    }
//                }
//
//            } else if (position == -1 || position == -2 || position == 1) {
//
//                mRotateAnim = (ObjectAnimator) view.getTag(R.id.tag_animator);
//                if (mRotateAnim != null) {
//                    mRotateAnim.setFloatValues(0);
//                    mRotateAnim.end();
//                    mRotateAnim = null;
//                }
//            } else {
//
//                if (mNeedleAnim != null) {
//                    mNeedleAnim.reverse();
//                    mNeedleAnim.end();
//                }
//
//                mRotateAnim = (ObjectAnimator) view.getTag(R.id.tag_animator);
//                if (mRotateAnim != null) {
//                    mRotateAnim.cancel();
//                    float valueAvatar = (float) mRotateAnim.getAnimatedValue();
//                    mRotateAnim.setFloatValues(valueAvatar, 360f + valueAvatar);
//
//                }
//            }
//        }
//
//    }
//
//    class FragmentAdapter extends FragmentStatePagerAdapter {
//
//        private int mChildCount = 0;
//
//        public FragmentAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//
//            if (position == MusicPlayer.getQueue().length + 1 || position == 0) {
//                return RoundFragment.newInstance("");
//            }
//            // return RoundFragment.newInstance(MusicPlayer.getQueue()[position - 1]);
//            return RoundFragment.newInstance(MusicPlayer.getAlbumPathAll()[position - 1]);
//        }
//
//        @Override
//        public int getCount() {
//            //左右各加一个
//            return MusicPlayer.getQueue().length + 2;
//        }
//
//
//        @Override
//        public void notifyDataSetChanged() {
//            mChildCount = getCount();
//            super.notifyDataSetChanged();
//        }
//
//        @Override
//        public int getItemPosition(Object object) {
//            if (mChildCount > 0) {
//                mChildCount--;
//                return POSITION_NONE;
//            }
//            return super.getItemPosition(object);
//        }
//
//    }

}
