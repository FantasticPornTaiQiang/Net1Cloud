package com.example.net1cloud.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.net1cloud.R;
import com.example.net1cloud.data.FragmentMsg;
import com.example.net1cloud.utils.PermissionUtil;
import com.example.net1cloud.utils.SaveImageUtil;

import org.greenrobot.eventbus.EventBus;

public class SaveAlbumImageFragment extends Fragment {

    private ImageView albumImageView;
    private LinearLayout saveAlbumImageLayout;
    private Bitmap albumImage;

    public SaveAlbumImageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save_album_image, container, false);
        view.setOnClickListener(view1 -> EventBus.getDefault().post(FragmentMsg.
                getInstance("SaveAlbumImageFragment", getString(R.string.hideFragment))));
        albumImageView = view.findViewById(R.id.album_image);
        saveAlbumImageLayout = view.findViewById(R.id.save_album_image_layout);
        saveAlbumImageLayout.setOnClickListener(view1 -> {
            PermissionUtil.verifyStoragePermissions((Activity)getContext());
            SaveImageUtil.saveImageToGallery(getContext(), albumImage);
            EventBus.getDefault().post(FragmentMsg.
                    getInstance("SaveAlbumImageFragment", getString(R.string.hideFragment)));
            Toast.makeText(getContext(), "保存成功", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    public void setAlbumImage(Bitmap albumImage) {
        this.albumImage = albumImage;
        albumImageView.setImageBitmap(albumImage);
    }
}