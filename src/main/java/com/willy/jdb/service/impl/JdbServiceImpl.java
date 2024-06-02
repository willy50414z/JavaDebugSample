package com.willy.jdb.service.impl;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;
import com.willy.jdb.request.ConnectRequest;
import com.willy.jdb.request.ToggleBreakPointRequest;
import com.willy.jdb.response.GetCurrentPositionResponse;
import com.willy.jdb.service.JdbService;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JdbServiceImpl implements JdbService {

  private VirtualMachine vm;
  private EventRequestManager erm;

  public void connect(ConnectRequest connectReq) {
    VirtualMachineManager vmManager = Bootstrap.virtualMachineManager();
    AttachingConnector connector = null;
    for (AttachingConnector c : vmManager.attachingConnectors()) {
      if ("dt_socket".equals(c.transport().name())) {
        connector = c;
        break;
      }
    }

    if (connector == null) {
      log.error("No socket attaching connector found");
      return;
    }

    Map<String, Argument> arguments = connector.defaultArguments();
    arguments.get("hostname").setValue(connectReq.getHost());
    arguments.get("port").setValue(connectReq.getPort());

    try {
      this.vm = connector.attach(arguments);
      this.erm = vm.eventRequestManager();
      log.debug("connect to debug jvm success");
    } catch (Exception e) {
      log.error("connect to jdb fail", e);
    }
  }

  @Override
  public void toggleBreakPoint(ToggleBreakPointRequest toggleBreakPointRequest) {
    try {
      List<ReferenceType> classes = this.vm.classesByName(toggleBreakPointRequest.getClazzName());
      // Check if the classes were found
      if (!classes.isEmpty()) {
        // Get the first loaded class (assuming there's only one)
        ReferenceType targetClass = classes.get(0);

        // Get a location in the target class where you want to set a breakpoint
        List<Location> locations =
            targetClass.locationsOfLine(
                toggleBreakPointRequest.getLineNum()); // Set breakpoint at line 10

        // Check if the locations were found
        if (!locations.isEmpty()) {
          Location location = locations.get(0);

          // Create a BreakpointRequest for the location
          BreakpointRequest breakpointRequest = erm.createBreakpointRequest(location);

          if (breakpointRequest.isEnabled()) {
            breakpointRequest.disable();
            log.debug(
                "success disable breakpoint, class[{}], lineNum[{}]",
                toggleBreakPointRequest.getClazzName(),
                toggleBreakPointRequest.getLineNum());
          } else {
            breakpointRequest.enable();
            log.debug(
                "success enable breakpoint, class[{}], lineNum[{}]",
                toggleBreakPointRequest.getClazzName(),
                toggleBreakPointRequest.getLineNum());
          }
        } else {
          //        return "Could not find line "+toggleBreakPointRequest.getLineNum()+" in
          // TargetApp";
        }

      } else {
        System.out.println("TargetApp class not found");
      }
    } catch (Exception e) {
      log.error("toggle breakpoint fail", e);
    }
  }

  @Override
  public GetCurrentPositionResponse getCurrentPosition() {
    EventSet eventSet;
    StringBuilder sb = new StringBuilder();
    EventQueue eventQueue = this.vm.eventQueue();
    boolean connected = true;
    while (connected) {
      try {
        eventSet = eventQueue.remove();
        for (Event event : eventSet) {
          if (event instanceof BreakpointEvent) {
            BreakpointEvent bpEvent = (BreakpointEvent) event;
            Location location = bpEvent.location();
            ReferenceType refType = location.declaringType();
            String className = refType.name();
            int lineNumber = location.lineNumber();

            // Print the paused line
            sb.append("Paused at line ")
                .append(lineNumber)
                .append(" in ")
                .append(className)
                .append("\r\n");
            System.out.println(sb.toString());

            // Get and print the call stack trace
            ThreadReference thread = bpEvent.thread();
            List<StackFrame> stackFrames = thread.frames();
            sb.append("Call stack trace:\r\n");
            for (StackFrame frame : stackFrames) {
              Location frameLocation = frame.location();
              ReferenceType frameRefType = frameLocation.declaringType();
              String frameClassName = frameRefType.name();
              int frameLineNumber = frameLocation.lineNumber();
              sb.append("  - ")
                  .append(frameClassName)
                  .append(":")
                  .append(frameLineNumber)
                  .append("\r\n");
              System.out.println(sb.toString());
            }
          } else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
            connected = false;
          }
        }
        eventSet.resume();
      } catch (Exception e) {
        log.error("getCurrentPosition throw exception", e);
        return GetCurrentPositionResponse.builder().position(sb.toString()).build();
      }
    }
    return GetCurrentPositionResponse.builder().position(sb.toString()).build();
  }

  @Override
  public void setpIn() {}

  @Override
  public void setpOver() {}

  @Override
  public void resume() {}
}
