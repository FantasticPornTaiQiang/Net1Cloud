package com.example.net1cloud.fragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.net1cloud.R;
import com.example.net1cloud.activity.LocalMusicActivity;
import com.example.net1cloud.activity.PlayingActivity;
import com.example.net1cloud.data.FragmentMsg;
import com.example.net1cloud.utils.MusicInfoUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.Objects;

public class LocalMusicChooseFragment extends Fragment {

    private ImageView localMusicAllChooseBox;
    private TextView localMusicChooseComplete;

    private boolean isSelectAll = false;

    public LocalMusicChooseFragment() {

    }

    public void setSelectAll(boolean isSelectAll) {
        this.isSelectAll = isSelectAll;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetFragmentChangeMessage(FragmentMsg fragmentMsg) {
        if ("LocalMusicChooseFragment".equals(fragmentMsg.getWhatFragment())) {
            if (fragmentMsg.getMsgString().equals(getString(R.string.selectAll))) {
                isSelectAll = true;
                localMusicAllChooseBox.setImageResource(R.drawable.nact_icn_choosed);
            } else if (fragmentMsg.getMsgString().equals(getString(R.string.notSelectAll))) {
                isSelectAll = false;
                localMusicAllChooseBox.setImageResource(R.drawable.nact_icn_unchoosed);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_music_choose, container, false);

        localMusicChooseComplete = view.findViewById(R.id.local_music_choose_complete);
        localMusicChooseComplete.setOnClickListener(view1 -> EventBus.getDefault().post(
                FragmentMsg.getInstance("LocalMusicChooseFragment", getString(R.string.complete))
        ));

        localMusicAllChooseBox = view.findViewById(R.id.local_all_choose_box);
        localMusicAllChooseBox.setOnClickListener(view1 -> {
            if(isSelectAll) {
                localMusicAllChooseBox.setImageResource(R.drawable.nact_icn_unchoosed);
                isSelectAll = false;
            }
            else {
                localMusicAllChooseBox.setImageResource(R.drawable.nact_icn_choosed);
                isSelectAll = true;
            }

            EventBus.getDefault().post(
                    FragmentMsg.getInstance("LocalMusicChooseFragment", getString(R.string.chooseAll)));
        });


        return view;
    }

}
