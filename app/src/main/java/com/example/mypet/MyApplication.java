package com.example.mypet;

import android.app.Application;
import org.osmdroid.config.Configuration;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
    }
}
