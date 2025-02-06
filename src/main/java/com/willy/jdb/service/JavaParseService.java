package com.willy.jdb.service;

import com.willy.jdb.dto.JavaFileInfoDto;
import com.willy.jdb.dto.MethodInfoDto;
import java.io.FileNotFoundException;
import java.util.List;

public interface JavaParseService {
  List<MethodInfoDto> getCallTreeInfoDto(
      String filePath, String methodName, List<String> paramTypeList) throws FileNotFoundException;

  JavaFileInfoDto parseFile(String filePath) throws FileNotFoundException;
}
