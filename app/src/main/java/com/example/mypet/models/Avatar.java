package com.example.mypet.models;

public class Avatar {

    private final int imageRes;
    private final String avatarKey;

    public Avatar(int imageRes, String avatarKey) {
        this.imageRes = imageRes;
        this.avatarKey = avatarKey;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getAvatarKey() {
        return avatarKey;
    }
}