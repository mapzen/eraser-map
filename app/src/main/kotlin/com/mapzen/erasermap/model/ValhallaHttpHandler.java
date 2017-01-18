package com.mapzen.erasermap.model;

import com.mapzen.android.routing.TurnByTurnHttpHandler;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class ValhallaHttpHandler extends TurnByTurnHttpHandler {

  public ValhallaHttpHandler(String endpoint, HttpLoggingInterceptor.Level logLevel) {
    configure(endpoint, logLevel);
  }

  public ValhallaHttpHandler(HttpLoggingInterceptor.Level logLevel) {
    configure(DEFAULT_URL, logLevel);
  }

  @Override
  protected Response onRequest(Interceptor.Chain chain) throws IOException {
    final Request request = chain.request()
        .newBuilder()
        .addHeader(Http.HEADER_DNT, Http.VALUE_HEADER_DNT)
        .build();
    return chain.proceed(request);
  }
}
