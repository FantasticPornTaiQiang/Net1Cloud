package com.example.net1cloud.utils;

import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class ToastUtil {

    public static void showMyToast(final Toast toast, final int time) {
        final Timer timer =new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        },0);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, time);
    }
}
