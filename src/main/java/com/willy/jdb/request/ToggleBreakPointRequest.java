package com.willy.jdb.request;

import lombok.Data;

@Data
public class ToggleBreakPointRequest {
  private String clazzName;
  private int lineNum;
}
