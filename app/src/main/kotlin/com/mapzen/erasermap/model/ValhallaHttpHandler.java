package com.mapzen.erasermap.model;

import com.mapzen.android.routing.MapzenRouterHttpHandler;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class ValhallaHttpHandler extends MapzenRouterHttpHandler {

  Map<String, String> headers = new HashMap() {
    {
      put(Http.HEADER_DNT, Http.VALUE_HEADER_DNT);
    }
  };

  public ValhallaHttpHandler(Context context, String url, LogLevel logLevel) {
    super(context, url, logLevel);
  }

  @Override public Map<String, String> queryParamsForRequest() {
    return null;
  }

  @Override public Map<String, String> headersForRequest() {
    return headers;
  }
}
