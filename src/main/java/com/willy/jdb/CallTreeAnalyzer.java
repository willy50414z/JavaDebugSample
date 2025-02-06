package com.willy.jdb;

import com.willy.jdb.dto.CalledMethodDto;
import com.willy.jdb.dto.VariableDto;
import com.willy.jdb.service.impl.JavaParseServiceImpl;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallTreeAnalyzer {
  static List<VariableDto> variableDtoList = new ArrayList<>();
  static List<CalledMethodDto> calledMethodDtoList = new ArrayList<>();
  static Map<String, String> typeAndFullPathMap = new HashMap<>();

  public static void main(String[] args) throws FileNotFoundException {
    new JavaParseServiceImpl()
        .getCallTreeInfoDto(
            "E:\\tmp\\shoalter-ecommerce-business-cart-service\\src\\main\\java\\com\\shoalter\\ecommerce\\cartservice\\service\\impl\\CartServiceImpl.java",
            "testCallTree",
            Arrays.asList("List<Wishlist2EntryDto>"));
  }
}
