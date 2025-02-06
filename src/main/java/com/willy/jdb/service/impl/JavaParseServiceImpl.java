package com.willy.jdb.service.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.willy.jdb.dto.CalledMethodDto;
import com.willy.jdb.dto.JavaFileInfoDto;
import com.willy.jdb.dto.MethodInfoDto;
import com.willy.jdb.dto.RangeDto;
import com.willy.jdb.dto.VariableDto;
import com.willy.jdb.service.JavaParseService;
import com.willy.jdb.visitor.ClassMethodsInfoVisitor;
import com.willy.jdb.visitor.InterfaceMethodsInfoVisitor;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class JavaParseServiceImpl implements JavaParseService {
  private List<VariableDto> variableDtoList = new ArrayList<>();
  private List<CalledMethodDto> calledMethodDtoList = new ArrayList<>();
  private int methodEndLine = 0;
  private String targetClassName;
  private Map<String, String> typeAndFullPathMap =
      new HashMap<>() {
        {
          put("String", "java.lang.String");
          put("int", "java.lang.Integer");
          put("long", "java.lang.Long");
          put("double", "java.lang.Double");
          put("float", "java.lang.Float");
          put("boolean", "java.lang.Boolean");
          put("Integer", "java.lang.Integer");
          put("Long", "java.lang.Long");
          put("Double", "java.lang.Double");
          put("Float", "java.lang.Float");
          put("Boolean", "java.lang.Boolean");
          put("System", "");
        }
      };

  @Override
  public List<MethodInfoDto> getCallTreeInfoDto(
      String filePath, String methodName, List<String> paramTypeList) throws FileNotFoundException {
    CompilationUnit cu = analyzeFile(filePath);

    // 蒐集呼叫的所有方法
    loadClazzInfo(cu);
    loadMethodInfo(cu, methodName, paramTypeList);

    // 確認被互叫的方法屬於哪個class
    fillUpVariableTypeFullPath();
    checkAllMethodScopeType();

    // 蒐集需要進一步分析的方法
    calledMethodDtoList.sort(Comparator.comparing(CalledMethodDto::getStart));
    calledMethodDtoList.forEach(
        calledMethodDto ->
            System.out.println(
                calledMethodDto.getScope()
                    + "."
                    + calledMethodDto.getName()
                    + "("
                    + calledMethodDto.getArgNames()
                    + ")"));

    //    System.out.println("methodDtoList[" + calledMethodDtoList + "]");

    //    for (VariableDto variableDto : variableDtoList.stream().filter(v -> v.getTypeFullPath() ==
    // null).toList())
    //    {
    //      System.out.println(variableDto.getName());
    //    }
    variableDtoList.sort(Comparator.comparing(VariableDto::getName));
    variableDtoList.forEach(variableDto -> System.out.println(variableDto.getName()));
    Map<String, String> variableAndTypePathMap =
        variableDtoList.stream()
            .collect(Collectors.toMap(VariableDto::getName, VariableDto::getTypeFullPath));
    System.out.println("呼叫的方法");
    for (CalledMethodDto calledMethodDto : calledMethodDtoList) {
      System.out.println(
          getTypeFullPath(calledMethodDto.getScope(), variableAndTypePathMap)
              + " "
              + calledMethodDto.getScope()
              + "."
              + calledMethodDto.getName()
              + "("
              + calledMethodDto.getArgNames()
              + ")");
    }

    return List.of();
  }

  @Override
  public JavaFileInfoDto parseFile(String filePath) throws FileNotFoundException {
    JavaFileInfoDto result = new JavaFileInfoDto();
    result.setPath(filePath);

    // class info
    CompilationUnit cu = analyzeFile(filePath);
    Optional<ClassOrInterfaceDeclaration> classDeclaration =
        cu.findFirst(ClassOrInterfaceDeclaration.class);
    result.setClassName(classDeclaration.get().getNameAsString());
    result.setRange(
        RangeDto.builder()
            .start(classDeclaration.get().getRange().get().begin.line)
            .end(classDeclaration.get().getRange().get().end.line)
            .build());

    // method info
    List<MethodInfoDto> methodInfoDtoList = new ArrayList<>();
    cu.accept(new ClassMethodsInfoVisitor(), methodInfoDtoList);
    cu.accept(new InterfaceMethodsInfoVisitor(), methodInfoDtoList);
    result.setMethodList(methodInfoDtoList);
    return result;
  }

  private String getTypeFullPath(String scope, Map<String, String> variableAndTypePathMap) {
    if (scope.contains(".")) {
      int aa = 0;
      //      String aa = "com.shoalter.ecommerce.cartservice.config.Constants.CURRENCY_BASE_FLAG";
      //      String className = aa.substring(0, aa.lastIndexOf("."));
      //      String variableName = aa.substring(aa.lastIndexOf(".")+1);
      //
      // System.out.println(Class.forName(className).getDeclaredField(variableName).getType().getName());

      //      @Autowired
      //      ApplicationContext applicationContext;
      //      @Override
      //      public void run(String... args) throws Exception {
      ////    Class.forName("com.shoalter.ecommerce.cartservice.service.impl.CheckoutServiceImpl");
      ////
      // Class.forName("com.shoalter.ecommerce.cartservice.service.impl.CheckoutServiceImpl").getClass();
      ////
      // Class.forName("com.shoalter.ecommerce.cartservice.service.impl.CheckoutServiceImpl").getClass().getClass();
      //        Object cl =
      // applicationContext.getBean(Class.forName("com.shoalter.ecommerce.cartservice.service.impl.CheckoutServiceImpl"));
      //        Field f = cl.getClass().getDeclaredField("grpcProductClient");
      //        f.setAccessible(true);
      //        System.out.println(f.get(cl).getClass().getTypeName());
      //      }
    }
    scope = scope.contains(".") ? scope.substring(0, scope.indexOf(".")) : scope;
    return Objects.toString(variableAndTypePathMap.get(scope), typeAndFullPathMap.get(scope));
  }

  private void fillUpVariableTypeFullPath() {
    for (VariableDto variableDto : variableDtoList) {
      String typeSimpleName =
          variableDto.getType().contains("<")
              ? variableDto.getType().substring(0, variableDto.getType().indexOf("<"))
              : variableDto.getType();
      if (variableDto.getName().equals("super") && typeAndFullPathMap.get(typeSimpleName) == null) {
        variableDto.setTypeFullPath(
            typeAndFullPathMap
                    .get(targetClassName)
                    .substring(0, typeAndFullPathMap.get(targetClassName).lastIndexOf(".") + 1)
                + variableDto.getType());
      } else {
        variableDto.setTypeFullPath(typeAndFullPathMap.get(typeSimpleName));
      }
      //      System.out.println(
      //            variableDto.getType()
      //                  + " "
      //                  + variableDto.getName()
      //                  + " in "
      //                  + variableDto.getTypeFullPath());
    }
  }

  private void checkAllMethodScopeType() {
    for (CalledMethodDto calledMethodDto : calledMethodDtoList) {
      String fullPath =
          variableDtoList.stream()
              .filter(
                  var ->
                      var.getType().equals(calledMethodDto.getScope())
                          && var.getStart() <= calledMethodDto.getStart()
                          && var.getEnd() >= calledMethodDto.getEnd())
              .findFirst()
              .map(VariableDto::getTypeFullPath)
              .orElse(null);
      if (fullPath == null) {
        //        Scanner sc = new Scanner(System.in);
        System.out.println(
            "Please input variable type["
                + calledMethodDto.getScope()
                + "] in ["
                + variableDtoList.stream()
                    .filter(var -> var.getName().equals(""))
                    .findFirst()
                    .get()
                    .getType()
                + ":"
                + calledMethodDto.getStart()
                + "]: ");
        continue;
        //        String input = sc.nextLine();
        //        variableDtoList.add(VariableDto.builder().build());
        //        throw new IllegalArgumentException(
        //
        //            "can't find variable type,scope["
        //                + calledMethodDto.getScope()
        //                + "]start["
        //                + calledMethodDto.getStart()
        //                + "]end["
        //                + calledMethodDto.getEnd()
        //                + "]");
      }
      //      System.out.println(
      //          calledMethodDto.getScope()
      //              + "."
      //              + calledMethodDto.getName()
      //              + ":"
      //              + calledMethodDto.getStart());
    }
  }

  private void loadMethodInfo(CompilationUnit cu, String methodName, List<String> paramTypeList) {
    cu.accept(new SpecificMethodVisitor(methodName, paramTypeList), null);
  }

  private void loadImportPackages(CompilationUnit cu) {
    List<ImportDeclaration> imports = cu.getImports();
    for (ImportDeclaration importDecl : imports) {
      //      System.out.println(importDecl.getNameAsString());
      typeAndFullPathMap.put(
          importDecl.getNameAsString().substring(importDecl.getNameAsString().lastIndexOf(".") + 1),
          importDecl.getNameAsString().replace(";", "").replace("\n", ""));
    }
  }

  private void loadClazzInfo(CompilationUnit cu) {
    PackageDeclaration packageDeclaration = cu.getPackageDeclaration().orElse(null);
    Optional<ClassOrInterfaceDeclaration> classDeclaration =
        cu.findFirst(ClassOrInterfaceDeclaration.class);
    int classLineCount = 0;
    if (classDeclaration.isPresent() && packageDeclaration != null) {
      ClassOrInterfaceDeclaration classOrInterface = classDeclaration.get();

      // Get the class name
      targetClassName = classOrInterface.getNameAsString();
      classLineCount = classOrInterface.getEnd().get().line;
      typeAndFullPathMap.put(
          targetClassName, packageDeclaration.getNameAsString() + "." + targetClassName);
    }

    List<FieldDeclaration> fields = cu.findAll(FieldDeclaration.class);
    for (FieldDeclaration field : fields) {
      //      System.out.println(field);
      // field.getVariables().get(0).getType().asString()
      // field.getVariables().get(0).getName().asString()
      variableDtoList.add(
          VariableDto.builder()
              .name(field.getVariables().get(0).getName().asString())
              .type(field.getVariables().get(0).getType().asString())
              .start(field.getVariables().get(0).getBegin().get().line)
              .end(classLineCount)
              .build());
    }
    variableDtoList.add(
        VariableDto.builder()
            .name("this")
            .type(targetClassName)
            .start(0)
            .end(classLineCount)
            .build());
    variableDtoList.add(
        VariableDto.builder().name("").type(targetClassName).start(0).end(classLineCount).build());

    // load method param to variable list
    cu.accept(new ClassOrInterfaceDeclarationVisitor(), null);
  }

  // Visitor class to visit only the specified method
  private class SpecificMethodVisitor extends VoidVisitorAdapter<Void> {
    private final String targetMethodName;
    private final List<String> paramTypeList;

    public SpecificMethodVisitor(String targetMethodName, List<String> paramTypeList) {
      this.targetMethodName = targetMethodName;
      this.paramTypeList = paramTypeList;
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, Void arg) {
      super.visit(methodDeclaration, arg);

      // Check if the method name matches the specified target method
      if (methodDeclaration.getNameAsString().equals(targetMethodName)
          && checkMethodParams(methodDeclaration, paramTypeList)) {
        // 方法參數
        methodDeclaration
            .getParameters()
            .forEach(
                param -> {
                  variableDtoList.add(
                      VariableDto.builder()
                          .type(param.getTypeAsString())
                          .name(param.getName().asString())
                          .start(methodDeclaration.getRange().get().begin.line)
                          .end(methodDeclaration.getRange().get().end.line)
                          .build());
                });
        methodEndLine = methodDeclaration.getRange().get().end.line;

        // Visit for loops within this method
        methodDeclaration.accept(new ForEachLoopVisitor(), null);
        methodDeclaration.accept(new ForLoopVisitor(), null);

        methodDeclaration.accept(new LambdaVisitor(), null);

        // Visit method calls within this method
        methodDeclaration.accept(new MethodCallVisitor(), null);

        // 因為會包含for/foreach的變數，需要放在最後
        methodDeclaration.accept(new VariableDeclarationVisitor(), null);
      }
    }
  }

  private boolean checkMethodParams(MethodDeclaration methodDeclaration, List<String> paramTypes) {
    if (methodDeclaration.getParameters().size() == paramTypes.size()) {
      for (int i = 0; i < methodDeclaration.getParameters().size(); i++) {
        if (!methodDeclaration.getParameter(0).getType().asString().equals(paramTypes.get(i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private class ClassOrInterfaceDeclarationVisitor extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(ClassOrInterfaceDeclaration ciDeclaration, Void arg) {
      super.visit(ciDeclaration, arg);
      ciDeclaration
          .getExtendedTypes()
          .forEach(
              superClass -> {
                variableDtoList.add(
                    VariableDto.builder()
                        .type(superClass.getNameAsString())
                        .name("super")
                        .start(ciDeclaration.getRange().get().begin.line)
                        .end(ciDeclaration.getRange().get().end.line)
                        .build());
              });
    }
  }

  // Visitor class to visit for loop statements
  private class ForEachLoopVisitor extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(ForEachStmt forStmt, Void arg) {
      super.visit(forStmt, arg);
      VariableDto variable =
          VariableDto.builder()
              .type(forStmt.getVariable().getVariable(0).getType().toString())
              .name(forStmt.getVariable().getVariable(0).getName().toString())
              .start(forStmt.getRange().get().begin.line)
              .end(forStmt.getRange().get().end.line)
              .build();
      variableDtoList.add(variable);
      //      System.out.println("Find foreach variable: " + variable);
    }
  }

  private class LambdaVisitor extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(LambdaExpr lambdaExpr, Void arg) {
      super.visit(lambdaExpr, arg);
      String scope =
          ((MethodCallExpr) lambdaExpr.getParentNode().get()).getScope().get().toString();
      if (scope.contains(").map(")) {
        String rootNode = scope.substring(0, scope.indexOf("."));
        //        throw new IllegalArgumentException("aa");
        String node = scope.substring(scope.indexOf(".stream().") + 10);
        String lambdasMethod = node.substring(0, node.indexOf("("));
        if (lambdasMethod.equals("filter")) {}

      } else {
        String lambdasVar = scope.substring(0, scope.indexOf("."));
        String scopeType =
            variableDtoList.stream()
                .filter(var -> var.getName().equals(lambdasVar))
                .findFirst()
                .get()
                .getType();
        VariableDto variable =
            VariableDto.builder()
                .type(scopeType.substring(scopeType.indexOf("<") + 1, scopeType.lastIndexOf(">")))
                .name(lambdaExpr.getParameters().get(0).getName().asString())
                .start(lambdaExpr.getParameters().get(0).getRange().get().begin.line)
                .end(lambdaExpr.getParameters().get(0).getRange().get().end.line)
                .build();
        variableDtoList.add(variable);
      }

      //      System.out.println("Find foreach variable: ");
    }
  }

  private class ForLoopVisitor extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(ForStmt forStmt, Void arg) {
      super.visit(forStmt, arg);
      // forStmt.getVariable().getVariable(0).getType()
      // forStmt.getRange().get().begin.line
      // forStmt.getRange().get().end.line
      // forStmt.getVariable().getVariables().get(0).name
      VariableDto variable =
          VariableDto.builder()
              .type(
                  ((VariableDeclarator) forStmt.getInitialization().get(0).getChildNodes().get(0))
                      .getType()
                      .asString())
              .name(
                  ((VariableDeclarator) forStmt.getInitialization().get(0).getChildNodes().get(0))
                      .getName()
                      .asString())
              .start(forStmt.getRange().get().begin.line)
              .end(forStmt.getRange().get().end.line)
              .build();
      variableDtoList.add(variable);
      //      System.out.println("Find foreach variable: " + forStmt);
    }
  }

  // Visitor class to visit method call expressions
  private class MethodCallVisitor extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(MethodCallExpr methodCallExpr, Void arg) {
      super.visit(methodCallExpr, arg);
      // Print the method call
      List<String> argNames = new ArrayList<>();
      for (Expression argument : methodCallExpr.getArguments()) {
        argNames.add(argument.toString());
      }
      CalledMethodDto calledMethodDto =
          CalledMethodDto.builder()
              .scope(
                  methodCallExpr.getScope().isPresent()
                      ? methodCallExpr.getScope().get().toString()
                      : "")
              .name(methodCallExpr.getName().toString())
              .start(methodCallExpr.getRange().get().begin.line)
              .end(methodCallExpr.getRange().get().end.line)
              .argNames(argNames)
              .build();
      calledMethodDtoList.add(calledMethodDto);
      //      System.out.println("Find Method: " + calledMethodDto);
    }
  }

  private class VariableDeclarationVisitor extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(VariableDeclarationExpr vdExpr, Void arg) {
      super.visit(vdExpr, arg);

      vdExpr
          .getVariables()
          .forEach(
              variable -> {
                // 因為會包含for/foreach的變數，需要放在最後
                if (variableDtoList.stream()
                    .noneMatch(v -> v.getName().equals(variable.getNameAsString()))) {
                  variableDtoList.add(
                      VariableDto.builder()
                          .name(variable.getNameAsString())
                          .type(variable.getTypeAsString())
                          .start(variable.getRange().get().begin.line)
                          .end(methodEndLine)
                          .build());
                }
              });
    }
  }

  private CompilationUnit analyzeFile(String filePath) throws FileNotFoundException {
    // analyze class
    TypeSolver reflectionTypeSolver = new ReflectionTypeSolver();
    CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
    combinedSolver.add(reflectionTypeSolver);

    // Create the JavaSymbolSolver using the TypeSolver
    JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);

    // Set up the ParserConfiguration with the symbol solver
    ParserConfiguration parserConfiguration =
        new ParserConfiguration().setSymbolResolver(symbolSolver);

    // Configure the JavaParser with the parser configuration
    JavaParser javaParser = new JavaParser(parserConfiguration);

    // Parse the target Java file
    return javaParser.parse(new File(filePath)).getResult().orElseThrow();
  }
}
