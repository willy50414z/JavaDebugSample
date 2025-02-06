package com.willy.jdb.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MethodInfoDto {
  private List<VariableDto> variableDtoList;
  private List<CalledMethodDto> calledMethodDtoList;
  private Map<String, String> typeAndFullPathMap;
  private List<AnnotationDto> annotationList;
  private String name;
  private RangeDto range;
}
