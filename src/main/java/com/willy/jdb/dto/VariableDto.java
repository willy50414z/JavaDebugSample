package com.willy.jdb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VariableDto {
  private String type;
  private String typeFullPath;
  private String name;
  private int start;
  private int end;
}
