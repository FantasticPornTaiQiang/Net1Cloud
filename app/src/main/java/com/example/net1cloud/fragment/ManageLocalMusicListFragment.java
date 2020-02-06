package com.example.net1cloud.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.net1cloud.R;
import com.example.net1cloud.data.FragmentMsg;

import org.greenrobot.eventbus.EventBus;

public class ManageLocalMusicListFragment extends Fragment {

    private LinearLayout localMusicAddListView;
    private LinearLayout localMusicDeleteView;

    public ManageLocalMusicListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_local_music_list, container, false);

        localMusicAddListView = view.findViewById(R.id.local_music_add_list_view);
        localMusicAddListView.setOnClickListener(view1 -> {

        });


        localMusicDeleteView = view.findViewById(R.id.local_music_delete_view);
        localMusicDeleteView.setOnClickListener(view1 -> {
            EventBus.getDefault().post(FragmentMsg.getInstance(
                    "ManageLocalMusicListFragment", getString(R.string.deleteSong)
            ));
        });

        return view;
    }

}
