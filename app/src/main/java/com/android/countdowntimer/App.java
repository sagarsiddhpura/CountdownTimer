package com.android.countdowntimer;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;
import io.paperdb.Paper;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(this);
        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());
    }
}
