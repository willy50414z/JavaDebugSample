package com.willy.jdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.willy.jdb.service.JavaParseService;
import com.willy.jdb.service.impl.JavaParseServiceImpl;

public class JavaFileParser {

  public static void main(String[] args) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    JavaParseService javaParseService = new JavaParseServiceImpl();
    args = new String[]{"E:/Code/shoalter-ecommerce-business/shoalter-ecommerce-business-cart-service\\src\\main\\java\\com\\shoalter\\ecommerce\\cartservice\\controller\\InternalCartController.java"};
    System.out.println(objectMapper.writeValueAsString(javaParseService.parseFile(args[0])));
  }
}
