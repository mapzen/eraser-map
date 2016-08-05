package com.mapzen.erasermap.model;

import com.mapzen.android.routing.TurnByTurnHttpHandler;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class ValhallaHttpHandler extends TurnByTurnHttpHandler {

  public ValhallaHttpHandler(String endpoint, RestAdapter.LogLevel logLevel) {
    configure(endpoint, logLevel);
  }

  public ValhallaHttpHandler(RestAdapter.LogLevel logLevel) {
    configure(DEFAULT_URL, logLevel);
  }

  @Override
  protected void onRequest(RequestInterceptor.RequestFacade requestFacade) {
    super.onRequest(requestFacade);
    requestFacade.addHeader(Http.HEADER_DNT, Http.VALUE_HEADER_DNT);
  }
}
