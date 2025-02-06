// package com.willy.jdb.controller;
//
// import com.sun.jdi.AbsentInformationException;
// import com.sun.jdi.IncompatibleThreadStateException;
// import com.willy.jdb.request.ConnectRequest;
// import com.willy.jdb.request.ToggleBreakPointRequest;
// import com.willy.jdb.response.GetCurrentPositionResponse;
// import com.willy.jdb.service.JdbService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.ResponseBody;
//
// @Controller
// @RequestMapping("/jdb")
// public class JdbController {
//  @Autowired private JdbService jdbSvc;
//
//  @PostMapping(value = "/connect")
//  @ResponseBody
//  public String connect(@RequestBody ConnectRequest connectReq) {
//    return jdbSvc.connect(connectReq);
//  }
//
//  @PostMapping(value = "/toggleBreakPoint")
//  @ResponseBody
//  public String toggleBreakPoint(@RequestBody ToggleBreakPointRequest toggleBreakPointReq) {
//    return jdbSvc.toggleBreakPoint(toggleBreakPointReq);
//  }
//
//  @GetMapping(value = "/getCurrentPosition")
//  @ResponseBody
//  public GetCurrentPositionResponse getCurrentPosition() {
//    return jdbSvc.getCurrentPosition();
//  }
//
//  @PostMapping(value = "/disconnect")
//  @ResponseBody
//  public String disconnect() {
//    return jdbSvc.disconnect();
//  }
//
//  @PostMapping(value = "/stepInto")
//  public void stepInto() {
//    jdbSvc.stepInto();
//  }
//
//  @PostMapping(value = "/stepOver")
//  public void stepOver() {
//    jdbSvc.stepOver();
//  }
//
//  @GetMapping(value = "/VariableValue/{varName}")
//  @ResponseBody
//  public String getVariableValue(@PathVariable("varName") String varName)
//      throws AbsentInformationException, IncompatibleThreadStateException {
//    return jdbSvc.getVariableValue(varName);
//  }
// }
