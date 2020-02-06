package com.example.net1cloud.utils;

import java.io.File;

public class DeleteSongsUtil {

    public static boolean deleteSongs(File file) {
        if (!file.exists()) {
            return false;
        }

        return file.delete();
    }
}
