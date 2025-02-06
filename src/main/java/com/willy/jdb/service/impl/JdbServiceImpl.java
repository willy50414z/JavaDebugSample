package com.willy.jdb.service.impl;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
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
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;
import com.willy.jdb.dto.PauseLineInfoDto;
import com.willy.jdb.request.ConnectRequest;
import com.willy.jdb.request.ToggleBreakPointRequest;
import com.willy.jdb.response.GetCurrentPositionResponse;
import com.willy.jdb.service.JdbService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JdbServiceImpl implements JdbService {

  private VirtualMachine vm;
  private EventRequestManager erm;

  private ThreadReference thread;

  public String connect(ConnectRequest connectReq) {
    VirtualMachineManager vmManager = Bootstrap.virtualMachineManager();
    AttachingConnector connector = null;
    for (AttachingConnector c : vmManager.attachingConnectors()) {
      if ("dt_socket".equals(c.transport().name())) {
        connector = c;
        break;
      }
    }

    if (connector == null) {
      //      log.error("No socket attaching connector found");
      return "No socket attaching connector found";
    }

    Map<String, Argument> arguments = connector.defaultArguments();
    arguments.get("hostname").setValue(connectReq.getHost());
    arguments.get("port").setValue(connectReq.getPort());

    try {
      this.vm = connector.attach(arguments);
      this.erm = vm.eventRequestManager();
      //      log.debug("connect to debug jvm success");
      return "success";
    } catch (Exception e) {
      //      log.error("connect to jdb fail, connectReq[{}]", connectReq, e);
      return "connect to jdb fail, connectReq[" + connectReq + "]";
    }
  }

  @Override
  public String disconnect() {
    if (this.vm != null) {
      this.vm.dispose();
      this.vm = null;
      this.erm = null;
    }
    return "success";
  }

  @Override
  public String getVariableValue(String varName)
      throws AbsentInformationException, IncompatibleThreadStateException {
    StackFrame frame = this.thread.frame(0);
    LocalVariable variable = frame.visibleVariableByName(varName);
    return frame.getValue(variable).toString();
  }

  @Override
  public String toggleBreakPoint(ToggleBreakPointRequest toggleBreakPointRequest) {
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
          breakpointRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
          if (breakpointRequest.isEnabled()) {
            breakpointRequest.disable();
            //            log.debug(
            //                "success disable breakpoint, class[{}], lineNum[{}]",
            //                toggleBreakPointRequest.getClazzName(),
            //                toggleBreakPointRequest.getLineNum());
            return "success disable breakpoint";
          } else {
            breakpointRequest.enable();
            //            log.debug(
            //                "success enable breakpoint, class[{}], lineNum[{}]",
            //                toggleBreakPointRequest.getClazzName(),
            //                toggleBreakPointRequest.getLineNum());
            return "success enable breakpoint";
          }
        } else {
          return toggleBreakPointRequest.getClazzName()
              + ":"
              + toggleBreakPointRequest.getLineNum()
              + " line not found";
        }
      } else {
        return toggleBreakPointRequest.getClazzName()
            + ":"
            + toggleBreakPointRequest.getLineNum()
            + " class not found";
      }
    } catch (Exception e) {
      //      log.error("toggle breakpoint fail", e);
      return "toggle breakpoint fail["
          + toggleBreakPointRequest.getClazzName()
          + ":"
          + toggleBreakPointRequest.getLineNum()
          + "]";
    }
  }

  @Override
  public GetCurrentPositionResponse getCurrentPosition() {
    try {
      return GetCurrentPositionResponse.builder().pauseLineInfo(processEvents(this.vm)).build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private PauseLineInfoDto processEvents(VirtualMachine vm) throws Exception {
    EventQueue eventQueue = vm.eventQueue();
    boolean connected = true;
    while (connected) {
      EventSet eventSet = eventQueue.remove();
      //      log.info("eventSet: {}", eventSet.size());
      for (Event event : eventSet) {
        if (event instanceof BreakpointEvent) {
          PauseLineInfoDto pauseLineInfoDto = handleBreakpointEvent((BreakpointEvent) event);
          //          log.info("pauseLineInfoDto: {}", pauseLineInfoDto);
          //          return pauseLineInfoDto;
        } else if (event instanceof StepEvent) {
          handleStepEvent((StepEvent) event);
        } else if (event instanceof VMDisconnectEvent) {
          //          log.info("VM disconnected.");
          connected = false;
        } else if (event instanceof VMDeathEvent) {
          //          log.info("VM died.");
          connected = false;
        } else if (event instanceof VMStartEvent) {
          //          log.info("VM started.");
          connected = false;
        }
        Thread.sleep(5000);
      }
    }
    //    eventSet.resume();
    return null;
  }

  private void handleStepEvent(StepEvent event) {
    try {
      StackFrame frame = this.thread.frame(0);
      Location location = frame.location();
      System.out.println("Paused at: " + location.sourceName() + ":" + location.lineNumber());
      List<StackFrame> frames = this.thread.frames();
      for (StackFrame f : frames) {
        System.out.println(f.location());
      }
      this.thread = event.thread();
    } catch (IncompatibleThreadStateException | AbsentInformationException e) {
      e.printStackTrace();
    }
  }

  private PauseLineInfoDto handleBreakpointEvent(BreakpointEvent event) {
    PauseLineInfoDto pauseLineInfoDto = PauseLineInfoDto.builder().build();
    this.thread = event.thread();
    try {
      StackFrame frame = thread.frame(0);
      Location location = frame.location();
      pauseLineInfoDto.setPauseLine(location.sourceName() + ":" + location.lineNumber());
      pauseLineInfoDto.setStackTrace(
          thread.frames().stream().map(f -> f.location().toString()).collect(Collectors.toList()));
      return pauseLineInfoDto;
    } catch (IncompatibleThreadStateException | AbsentInformationException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public void stepInto() {
    clearStepRequests(thread);
    EventRequestManager erm = vm.eventRequestManager();
    StepRequest stepRequest =
        erm.createStepRequest(thread, StepRequest.STEP_LINE, StepRequest.STEP_INTO);
    stepRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
    stepRequest.enable();
    vm.resume();
  }

  @Override
  public void stepOver() {
    clearStepRequests(thread);
    EventRequestManager erm = vm.eventRequestManager();
    StepRequest stepRequest =
        erm.createStepRequest(thread, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
    stepRequest.setSuspendPolicy(EventRequest.SUSPEND_ALL);
    stepRequest.enable();
    vm.resume();
  }

  private void clearStepRequests(ThreadReference thread) {
    EventRequestManager erm = vm.eventRequestManager();
    List<StepRequest> stepRequests = erm.stepRequests();
    for (StepRequest request : stepRequests) {
      if (request.thread().equals(thread)) {
        erm.deleteEventRequest(request);
      }
    }
  }

  @Override
  public void setpIn() {}

  @Override
  public void setpOver() {}

  @Override
  public void resume() {}
}
