package com.willy.jdb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RangeDto {
  private int start;
  private int end;
}
