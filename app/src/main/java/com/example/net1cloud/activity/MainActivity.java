package com.example.net1cloud.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.net1cloud.R;
import com.example.net1cloud.service.PlayMusicService;

public class MainActivity extends AppCompatActivity {

    private TextView getLocalMusicButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        startPlayMusicService();
    }

    private void initView() {
        getLocalMusicButton = findViewById(R.id.get_local_music_button);
        getLocalMusicButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LocalMusicActivity.class);
            startActivity(intent);
        });
    }

    private void startPlayMusicService(){
        Intent intent = new Intent(this, PlayMusicService.class);
        startService(intent);
    }
}
