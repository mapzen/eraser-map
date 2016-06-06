package com.mapzen.erasermap.model;

import com.mapzen.valhalla.HttpHandler;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class ValhallaHttpHandler extends HttpHandler {

  public ValhallaHttpHandler(String apiKey) {
    super(apiKey);
  }

  public ValhallaHttpHandler(String apiKey, RestAdapter.LogLevel logLevel) {
    super(apiKey, logLevel);
  }

  public ValhallaHttpHandler(String apiKey, String endpoint, RestAdapter.LogLevel logLevel) {
    super(apiKey, endpoint, logLevel);
  }

  protected void addHeadersForRequest(RequestInterceptor.RequestFacade requestFacade) {
    requestFacade.addHeader(Http.HEADER_DNT, Http.VALUE_HEADER_DNT);
  }
}
