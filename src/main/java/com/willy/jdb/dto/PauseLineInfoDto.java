package com.willy.jdb.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PauseLineInfoDto {
  private String pauseLine;
  private List<String> stackTrace;
}
