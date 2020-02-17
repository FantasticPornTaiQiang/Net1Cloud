package com.example.net1cloud.data;

public class LrcRow {

    //▶[00:03.01]Crashed heart and relation
    //[00:07.48]What is it to have faith?
    private String timeStr;//本行时间字符串 即00:03.01
    private int time;//本行歌词开始播放时间 即3010
    private int totalTime;//本行歌词持续时间 即7480-3010=4470
    private String content;//本行歌词内容 即Crashed heart and relation

    public String getTimeStr() { return timeStr; }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public String getContent() { return content; }

    public void setContent(String content) {
        this.content = content;
    }

    public LrcRow (String timeStr, String content, int time, int totalTime) {
        this.timeStr = timeStr;
        this.time = time;
        this.content = content;
        this.totalTime = totalTime;
    }

    public LrcRow () {

    }
}
