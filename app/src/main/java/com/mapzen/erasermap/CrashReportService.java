package com.mapzen.erasermap;

import com.mapzen.erasermap.model.ManifestModel;

import com.splunk.mint.Mint;

import android.content.Context;

public class CrashReportService {
    public void initAndStartSession(Context context, ManifestModel apiKeys) {
        if (apiKeys.mintApiKey!= null) {
            Mint.initAndStartSession(context, apiKeys.mintApiKey);
        }
    }
}
