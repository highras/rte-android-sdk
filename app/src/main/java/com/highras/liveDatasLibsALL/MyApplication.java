package com.highras.liveDatasLibsALL;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        CrashReport.initCrashReport(getApplicationContext(), "02d0e15c2e", true);
    }
}
