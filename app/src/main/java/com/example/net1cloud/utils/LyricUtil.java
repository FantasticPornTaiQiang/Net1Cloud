package com.example.net1cloud.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.net1cloud.data.LrcRow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LyricUtil {

    public static List<LrcRow> parseToLyricRowList(Context context, String musicName, int musicDuration) {
        List<LrcRow> lrcRowList = new ArrayList<>();
        List<String> lrcList = new ArrayList<>();
        String lrcPath = "";

        PermissionUtil.verifyStoragePermissions((Activity)context);
        File lrcDirectory = new File(Environment.getExternalStorageDirectory() + "/Net1Cloud", "lrc");
        if (!lrcDirectory.exists()) {
            Toast.makeText(context, "歌词导入失败，错误：文件夹未找到", Toast.LENGTH_SHORT).show();
        }
        File[] lrcFiles = lrcDirectory.listFiles();
        if(lrcFiles != null && lrcFiles.length > 0) {
            for (File lrcFile : lrcFiles) {
                if (lrcFile.getName().equals(musicName.trim() + ".lrc")) {
                    lrcPath = lrcFile.getPath();
                    break;
                }
            }
        }

        try {
            File lrcFile = new File(lrcPath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(lrcFile));
            String lrcLine;
            while(null != (lrcLine = bufferedReader.readLine())){
                if(lrcLine.trim().length() == 0)
                    continue;
                lrcList.add(lrcLine.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(lrcList.size() == 0) {
            return null;
        }

        String timeStr = lrcList.get(0).substring(1, 9);
        int time = (int)TimeUtil.getMilliSecondsFromLrcTimeStr(timeStr);
        for(int i = 0; i < lrcList.size(); i++) {
            LrcRow lrcRow = new LrcRow();
            lrcRow.setTimeStr(timeStr);
            lrcRow.setTime(time);
            String content = lrcList.get(i).substring(lrcList.get(i).indexOf("]") + 1);
            lrcRow.setContent(content);
            int totalTime = -time;
            if(i < lrcList.size() - 1) {
                timeStr = lrcList.get(i + 1).substring(1, 9);
                time = (int)TimeUtil.getMilliSecondsFromLrcTimeStr(timeStr);
                totalTime += time;
            } else {
                totalTime = musicDuration - time;
            }
            lrcRow.setTotalTime(totalTime);
            lrcRowList.add(lrcRow);
        }

        return lrcRowList.size() > 0 ? lrcRowList : null;
    }

}
