package com.willy.jdb.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetCurrentPositionResponse {
  private String position;
}
