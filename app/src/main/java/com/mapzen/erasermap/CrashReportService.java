package com.mapzen.erasermap;

import com.splunk.mint.Mint;

import android.content.Context;

public class CrashReportService {
    public void initAndStartSession(Context context) {
        if (!BuildConfig.DEBUG && BuildConfig.MINT_API_KEY != null) {
            Mint.initAndStartSession(context, BuildConfig.MINT_API_KEY);
        }
    }
}
