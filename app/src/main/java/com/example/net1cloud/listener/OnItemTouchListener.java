package com.example.net1cloud.listener;

public interface OnItemTouchListener {
    boolean onMove(int fromPosition, int toPosition);
    void onSwiped(int position);
}
