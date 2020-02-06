package com.example.net1cloud.fragment;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.net1cloud.R;
import com.example.net1cloud.data.FragmentMsg;

import org.greenrobot.eventbus.EventBus;

public class LocalMusicManageChooseFragment extends Fragment {

    private RelativeLayout playAllRelativeLayout;
    private LinearLayout manageLocalMusicListButton;

    public LocalMusicManageChooseFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_music_manage_choose, container, false);

        playAllRelativeLayout = view.findViewById(R.id.layout_play_all);
        playAllRelativeLayout.setOnClickListener(view1 -> EventBus.getDefault().post(
                FragmentMsg.getInstance("LocalMusicManageChooseFragment", getString(R.string.playAll))
        ));

        manageLocalMusicListButton = view.findViewById(R.id.manage_local_music_list_layout);
        manageLocalMusicListButton.setOnClickListener(view1 -> EventBus.getDefault().post(
                FragmentMsg.getInstance("LocalMusicManageChooseFragment", getString(R.string.manage))
        ));
        return view;
    }

}
