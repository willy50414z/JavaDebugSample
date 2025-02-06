package com.willy.jdb.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnnotationDto {
  String name;
  Map<String, List<String>> attrs;
}
