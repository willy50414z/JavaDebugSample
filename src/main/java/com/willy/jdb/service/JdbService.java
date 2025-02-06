package com.willy.jdb.service;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.willy.jdb.request.ConnectRequest;
import com.willy.jdb.request.ToggleBreakPointRequest;
import com.willy.jdb.response.GetCurrentPositionResponse;

public interface JdbService {
  String connect(ConnectRequest connectReq);

  String disconnect();

  String getVariableValue(String varName)
      throws AbsentInformationException, IncompatibleThreadStateException;

  String toggleBreakPoint(ToggleBreakPointRequest toggleBreakPointRequest);

  GetCurrentPositionResponse getCurrentPosition();

  void stepInto();

  void stepOver();

  void setpIn();

  void setpOver();

  void resume();
}
