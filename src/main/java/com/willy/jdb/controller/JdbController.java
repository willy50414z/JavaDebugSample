package com.willy.jdb.controller;

import com.willy.jdb.request.ConnectRequest;
import com.willy.jdb.request.ToggleBreakPointRequest;
import com.willy.jdb.response.GetCurrentPositionResponse;
import com.willy.jdb.service.JdbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/jdb")
public class JdbController {
  @Autowired private JdbService jdbSvc;

  @PostMapping(value = "/connect")
  public void connect(@RequestBody ConnectRequest connectReq) {
    jdbSvc.connect(connectReq);
  }

  @PostMapping(value = "/setBreakPoint")
  public void toggleBreakPoint(@RequestBody ToggleBreakPointRequest toggleBreakPointReq) {
    jdbSvc.toggleBreakPoint(toggleBreakPointReq);
  }

  @GetMapping(value = "/getCurrentPosition")
  public GetCurrentPositionResponse getCurrentPosition() {
    return jdbSvc.getCurrentPosition();
  }
}
