package com.example.net1cloud.adapter;

import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.net1cloud.R;
import com.example.net1cloud.data.Music;
import com.example.net1cloud.listener.OnItemClickListener;

import java.util.List;

public class LocalMusicRecycleViewAdapter extends RecyclerView.Adapter {

    public static class LocalMusicListViewHolder extends RecyclerView.ViewHolder{
        private final TextView localMusicNameTextView;
        private final ImageView localMusicisDownloadImageView;
        private final TextView localMusicAlbumNameTextView;
        private final ImageButton localMusicMenuImageButton;

        public LocalMusicListViewHolder(View itemView) {
            super(itemView);
            localMusicNameTextView = itemView.findViewById(R.id.local_music_name_text_view);
            localMusicNameTextView.setSingleLine(true);
            localMusicNameTextView.setEllipsize(TextUtils.TruncateAt.END);
            localMusicisDownloadImageView = itemView.findViewById(R.id.local_music_is_download_image_view);
            localMusicAlbumNameTextView = itemView.findViewById(R.id.local_music_album_name_text_view);
            localMusicAlbumNameTextView.setSingleLine(true);
            localMusicAlbumNameTextView.setEllipsize(TextUtils.TruncateAt.END);
            //textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            //            textView.setSingleLine(true);
            //            textView.setSelected(true);
            //            textView.setFocusable(true);
            //            textView.setFocusableInTouchMode(true);设置滚动
            localMusicMenuImageButton = itemView.findViewById(R.id.local_music_menu_image_button);
        }
    }

    private List<Music> localMusicInfoList;

    public LocalMusicRecycleViewAdapter(List<Music> localMusicInfoList) {
        this.localMusicInfoList = localMusicInfoList;
    }

    public void setLocalMusicInfoList(List<Music> localMusicInfoList) {
        this.localMusicInfoList = localMusicInfoList;
    }

    private OnItemClickListener myClickListener;
    public void setOnItemClickListener(OnItemClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    @Override
    public LocalMusicListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_music_item_layout, parent, false);
        return new LocalMusicListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        LocalMusicListViewHolder localMusicListViewHolder = (LocalMusicListViewHolder) viewHolder;
        localMusicListViewHolder.localMusicNameTextView.setText(localMusicInfoList.get(position).getName());
        localMusicListViewHolder.localMusicAlbumNameTextView.setText(localMusicInfoList.get(position).getArtist() + " - " + localMusicInfoList.get(position).getAlbum());
        localMusicListViewHolder.localMusicMenuImageButton.setOnClickListener(view -> {

        });

        localMusicListViewHolder.itemView.setOnClickListener(v -> {
            if (myClickListener != null) {
                myClickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return localMusicInfoList.size();
    }
}
