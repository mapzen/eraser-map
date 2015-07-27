package com.mapzen.erasermap;

import com.splunk.mint.Mint;

import android.content.Context;

public class CrashReportService {
    public void initAndStartSession(Context context) {
        Mint.initAndStartSession(context, BuildConfig.MINT_API_KEY);
    }
}
