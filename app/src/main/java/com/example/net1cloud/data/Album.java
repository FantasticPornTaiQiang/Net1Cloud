package com.example.net1cloud.data;

import java.util.ArrayList;
import java.util.List;

public class Album {

    private String name;

    public List<? extends Music> musicList = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
