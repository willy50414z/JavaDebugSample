package com.willy.jdb.visitor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.willy.jdb.dto.AnnotationDto;
import com.willy.jdb.dto.MethodInfoDto;
import com.willy.jdb.dto.RangeDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassMethodsInfoVisitor extends VoidVisitorAdapter<List<MethodInfoDto>> {
  @Override
  public void visit(MethodDeclaration methodDeclaration, List<MethodInfoDto> result) {
    super.visit(methodDeclaration, result);
    NodeList<AnnotationExpr> annotations = methodDeclaration.getAnnotations();
    List<AnnotationDto> annotationDtoList = new ArrayList<>();
    for (AnnotationExpr annotation : annotations) {
      HashMap<String, List<String>> annotationAttrsMap = new HashMap<>();
      annotation
          .getChildNodes()
          .forEach(
              childNode -> {
                try {
                  if (childNode instanceof MemberValuePair memberValue) {
                    List<String> attrValues;
                    if (memberValue.getValue() instanceof StringLiteralExpr stringLiteralExpr) {
                      attrValues = List.of(stringLiteralExpr.asString());
                    } else if (memberValue.getValue()
                        instanceof ArrayInitializerExpr arrayInitializerExpr) {
                      attrValues =
                          arrayInitializerExpr.getChildNodes().stream()
                              .map(
                                  node -> {
                                    if (node instanceof StringLiteralExpr stringLiteralExpr) {
                                      return stringLiteralExpr.asString();
                                    } else {
                                      return node.toString();
                                    }
                                  })
                              .toList();
                    } else if (memberValue.getValue() instanceof BinaryExpr binaryExpr) {
                      attrValues = List.of(binaryExpr.toString());
                    } else {
                      attrValues = List.of(memberValue.getValue().toString());
                    }
                    annotationAttrsMap.put(memberValue.getNameAsString(), attrValues);
                  }
                } catch (Exception e) {
                  throw new IllegalArgumentException(
                      "unexpect type ["
                          + childNode.getClass().getSimpleName()
                          + "]methodName["
                          + methodDeclaration.getNameAsString()
                          + "]annotation name["
                          + annotation.getNameAsString()
                          + "]");
                }
              });
      annotationDtoList.add(
          AnnotationDto.builder()
              .name(annotation.getNameAsString())
              .attrs(annotationAttrsMap)
              .build());
    }

    result.add(
        MethodInfoDto.builder()
            .name(methodDeclaration.getNameAsString())
            .range(
                RangeDto.builder()
                    .start(methodDeclaration.getRange().get().begin.line)
                    .end(methodDeclaration.getRange().get().end.line)
                    .build())
            .annotationList(annotationDtoList)
            .build());
  }
}
