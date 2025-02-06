package com.willy.jdb.response;

import com.willy.jdb.dto.PauseLineInfoDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetCurrentPositionResponse {
  private PauseLineInfoDto pauseLineInfo;
}
