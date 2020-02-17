package com.example.net1cloud.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.net1cloud.R;
import com.example.net1cloud.listener.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class PlayingMoreRecyclerViewAdapter extends RecyclerView.Adapter {

    public class PlayingMoreRecyclerViewHolder extends RecyclerView.ViewHolder{
        private final TextView playingMoreTextView;
        private final ImageView playingMoreImageView;

        PlayingMoreRecyclerViewHolder(View itemView) {
            super(itemView);
            playingMoreTextView = itemView.findViewById(R.id.playing_more_text_view);
            playingMoreImageView = itemView.findViewById(R.id.playing_more_image_view);
        }
    }

    private List<Integer> srcList;
    private List<String> textList;
    private String albumName;
    private String artist;
    private Context context;
    private String source;

    public PlayingMoreRecyclerViewAdapter(String artist, String albumName, String source, Context context) {
        this.srcList = new ArrayList<>();
        srcList.add(R.drawable.lay_icn_fav);//收藏到歌单
        srcList.add(R.drawable.lay_icn_artist);//歌手
        srcList.add(R.drawable.lay_icn_alb);//专辑
        srcList.add(R.drawable.lay_icn_share);//来源
        srcList.add(R.drawable.lay_icn_share);//鲸云特效
        srcList.add(R.drawable.lay_icn_upquality);//音质
        srcList.add(R.drawable.lay_icn_ring);//设为铃声
        srcList.add(R.drawable.lay_icn_next);//定时关闭
        srcList.add(R.drawable.lay_icn_share);//打开驾驶模式
        srcList.add(R.drawable.lay_icn_share);//屏蔽歌曲或歌手
        srcList.add(R.drawable.lay_icn_share);//举报
        this.textList = new ArrayList<>();
        textList.add(context.getString(R.string.collectToMusicList));
        textList.add(context.getString(R.string.artistName));
        textList.add(context.getString(R.string.albumName));
        textList.add(context.getString(R.string.source));
        textList.add(context.getString(R.string.net1CloudSpecialEffect));
        textList.add(context.getString(R.string.quality));
        textList.add(context.getString(R.string.setAsAlarm));
        textList.add(context.getString(R.string.timingOff));
        textList.add(context.getString(R.string.drivingMode));
        textList.add(context.getString(R.string.shieldMusicOrArtist));
        textList.add(context.getString(R.string.report));
        this.albumName = albumName;
        this.artist = artist;
        this.context = context;
        this.source = source;
    }

    private OnItemClickListener myClickListener;
    public void setOnItemClickListener(OnItemClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playing_more_item_layout, parent, false);
        return new PlayingMoreRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        PlayingMoreRecyclerViewHolder playingMoreRecyclerViewHolder = (PlayingMoreRecyclerViewHolder) viewHolder;
        playingMoreRecyclerViewHolder.playingMoreImageView.setImageResource(srcList.get(position));
        if(position == 1) playingMoreRecyclerViewHolder.playingMoreTextView.setText(
                String.format(context.getString(R.string.assembleChoiceInPlayingMode), textList.get(position), artist));
        else if(position == 2) playingMoreRecyclerViewHolder.playingMoreTextView.setText(
                String.format(context.getString(R.string.assembleChoiceInPlayingMode), textList.get(position), albumName));
        else if(position == 3) playingMoreRecyclerViewHolder.playingMoreTextView.setText(
                String.format(context.getString(R.string.assembleChoiceInPlayingMode), textList.get(position), source));
        else playingMoreRecyclerViewHolder.playingMoreTextView.setText(textList.get(position));
        playingMoreRecyclerViewHolder.itemView.setOnClickListener(view -> {
            if(myClickListener != null) {
                myClickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return srcList.size();
    }
}
