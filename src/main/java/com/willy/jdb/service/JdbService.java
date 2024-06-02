package com.willy.jdb.service;

import com.willy.jdb.request.ConnectRequest;
import com.willy.jdb.request.ToggleBreakPointRequest;
import com.willy.jdb.response.GetCurrentPositionResponse;

public interface JdbService {
  void connect(ConnectRequest connectReq);

  void toggleBreakPoint(ToggleBreakPointRequest toggleBreakPointRequest);

  GetCurrentPositionResponse getCurrentPosition();

  void setpIn();

  void setpOver();

  void resume();
}
