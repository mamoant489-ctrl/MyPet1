package com.example.mypet.models;

public class Avatar {

    private final int imageRes;
    private final String name;

    public Avatar(int imageRes, String name) {
        this.imageRes = imageRes;
        this.name = name;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getName() {
        return name;
    }
}