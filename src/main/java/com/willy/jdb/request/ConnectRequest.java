package com.willy.jdb.request;

import lombok.Data;

@Data
public class ConnectRequest {
  private String host;
  private String port;
}
