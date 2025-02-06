package com.willy.jdb.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalledMethodDto {
  private String scope;
  private String name;
  private List<String> argNames;
  private int start;
  private int end;
}
