package com.willy.jdb.dto;

import java.util.List;
import lombok.Data;

@Data
public class JavaFileInfoDto {
  private String path;
  private String className;
  private List<MethodInfoDto> methodList;
  private RangeDto range;
}
