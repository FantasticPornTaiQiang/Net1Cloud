package com.example.net1cloud.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.net1cloud.R;
import com.example.net1cloud.data.FragmentMsg;
import com.example.net1cloud.data.Music;
import com.example.net1cloud.listener.OnItemClickListener;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocalMusicRecycleViewAdapter extends RecyclerView.Adapter {

    private Context context;
    private Boolean[] isChoosedArray;
    private boolean isManaging;
    private boolean isSelectAll;

    public class LocalMusicListViewHolder extends RecyclerView.ViewHolder{
        private final TextView localMusicNameTextView;
        private final TextView localMusicAlbumNameTextView;
        private final ImageButton localMusicMenuImageButton;
        private final ImageView localMusicChooseBox;

        LocalMusicListViewHolder(View itemView) {
            super(itemView);
            localMusicNameTextView = itemView.findViewById(R.id.local_music_name_text_view);
            localMusicNameTextView.setSingleLine(true);
            localMusicNameTextView.setEllipsize(TextUtils.TruncateAt.END);
            localMusicAlbumNameTextView = itemView.findViewById(R.id.local_music_album_name_text_view);
            localMusicAlbumNameTextView.setSingleLine(true);
            localMusicAlbumNameTextView.setEllipsize(TextUtils.TruncateAt.END);
            //textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            //            textView.setSingleLine(true);
            //            textView.setSelected(true);
            //            textView.setFocusable(true);
            //            textView.setFocusableInTouchMode(true);设置滚动
            localMusicMenuImageButton = itemView.findViewById(R.id.local_music_menu_image_button);
            localMusicChooseBox = itemView.findViewById(R.id.local_music_choose_box);
            localMusicChooseBox.setVisibility(View.GONE);
        }
    }

    private List<Music> localMusicInfoList;

    public LocalMusicRecycleViewAdapter(List<Music> localMusicInfoList, Context context) {
        this.localMusicInfoList = localMusicInfoList;
        this.context = context;
        this.isChoosedArray = new Boolean[localMusicInfoList.size()];
        Arrays.fill(isChoosedArray, false);
    }

    public void setLocalMusicInfoList(List<Music> localMusicInfoList) {
        this.localMusicInfoList = localMusicInfoList;
    }

    private OnItemClickListener myClickListener;
    public void setOnItemClickListener(OnItemClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    @NotNull
    @Override
    public LocalMusicListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.local_music_item_layout, parent, false);
        return new LocalMusicListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder viewHolder, final int position) {
        LocalMusicListViewHolder localMusicListViewHolder = (LocalMusicListViewHolder) viewHolder;
        localMusicListViewHolder.localMusicNameTextView.setText(localMusicInfoList.get(position).getName());
        localMusicListViewHolder.localMusicAlbumNameTextView.setText(
                String.format(context.getString(R.string.localMusicAlbumName), localMusicInfoList.get(position).getArtist(),
                        localMusicInfoList.get(position).getAlbum()));
        localMusicListViewHolder.localMusicMenuImageButton.setOnClickListener(view -> {

        });

        if(isChoosedArray[position])
            localMusicListViewHolder.localMusicChooseBox.setImageResource(R.drawable.nact_icn_choosed);
        else
            localMusicListViewHolder.localMusicChooseBox.setImageResource(R.drawable.nact_icn_unchoosed);

        localMusicListViewHolder.itemView.setOnClickListener(v -> {
            if (myClickListener != null) {
                myClickListener.onClick(position);

                if(!isChoosedArray[position]) {
                    localMusicListViewHolder.localMusicChooseBox.setImageResource(R.drawable.nact_icn_choosed);
                    isChoosedArray[position] = true;
                }
                else {
                    localMusicListViewHolder.localMusicChooseBox.setImageResource(R.drawable.nact_icn_unchoosed);
                    isChoosedArray[position] = false;
                }

                if(isSelectAll) {
                    for (Boolean isChoosed : isChoosedArray) {
                        if (!isChoosed) {
                            isSelectAll = false;
                            EventBus.getDefault().post(FragmentMsg.getInstance("LocalMusicChooseFragment",
                                    context.getString(R.string.notSelectAll)));
                            break;
                        }
                    }
                } else {
                    for (Boolean isChoosed : isChoosedArray) {
                        if (!isChoosed) {
                            return;
                        }
                    }
                    isSelectAll = true;
                    EventBus.getDefault().post(FragmentMsg.getInstance("LocalMusicChooseFragment",
                            context.getString(R.string.selectAll)));
                }
            }
        });

        if(isManaging) {
            localMusicListViewHolder.localMusicChooseBox.setVisibility(View.VISIBLE);
            localMusicListViewHolder.localMusicMenuImageButton.setVisibility(View.GONE);
        }
        else {
            localMusicListViewHolder.localMusicChooseBox.setVisibility(View.GONE);
            localMusicListViewHolder.localMusicMenuImageButton.setVisibility(View.VISIBLE);
            localMusicListViewHolder.itemView.setOnLongClickListener(v -> {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager)  context.getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("Label", localMusicInfoList.get(position).getName());
                // 将ClipData内容放到系统剪贴板里。
                if (cm != null) {
                    cm.setPrimaryClip(mClipData);
                }

                Toast.makeText(context, "歌曲名已复制到剪切板", Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }

    public void setChooseBoxVisibility(boolean visible) {
        if(visible) {
            isChoosedArray = new Boolean[localMusicInfoList.size()];
            Arrays.fill(isChoosedArray, false);
            isSelectAll = false;
        }
        isManaging = visible;
    }

    public void setSelectAll() {
        if(!isSelectAll) {
            Arrays.fill(isChoosedArray, true);
            isSelectAll = true;
        } else {
            Arrays.fill(isChoosedArray, false);
            isSelectAll = false;
        }
    }

    public List<Integer> getSelectedSongsIndex() {
        if(isManaging) {
            List<Integer> selectedSongsUrlList = new ArrayList<>();
            for(int i = 0; i < isChoosedArray.length; i++) {
                if(isChoosedArray[i]) {
                    selectedSongsUrlList.add(i);
                }
            }
            return selectedSongsUrlList;
        }
        return null;
    }

    public boolean isSelectSongs() {
        for (Boolean isChoosed : isChoosedArray) {
            if (isChoosed) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return localMusicInfoList.size();
    }
}
