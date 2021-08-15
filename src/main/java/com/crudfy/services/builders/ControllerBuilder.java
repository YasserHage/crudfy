package com.crudfy.services.builders;

import com.crudfy.services.utils.ArgumentUtils;
import com.crudfy.services.utils.NameUtils;
import com.crudfy.services.utils.TypeUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ControllerBuilder extends ClassOrInterfaceBuilder{

    @Autowired
    private NameUtils nameUtils;

    @Autowired
    private TypeUtils typeUtils;

    @Autowired
    private ArgumentUtils argumentUtils;

    public void buildController(String controllerPath, String projectName) {

        String className = nameUtils.getControllerClassName(projectName) ;
        CompilationUnit compilationUnit = initialize("com." + projectName + ".controllers", className, false);
        ClassOrInterfaceDeclaration controllerClass = compilationUnit.getClassByName(className).get();

        addImports(Arrays.asList(
                nameUtils.getResourceImportPath(projectName),
                nameUtils.getResponseImportPath(projectName),
                nameUtils.getServiceImportPath(projectName),
                "org.springframework.beans.factory.annotation.Autowired",
                "org.springframework.web.bind.annotation.*",
                "org.springframework.stereotype.Controller",
                "org.springframework.http.ResponseEntity",
                "org.springframework.http.HttpStatus",
                "java.util.Optional",
                "java.util.List"
                ));

        addControllerAnnotations(controllerClass, projectName);
        addFields(controllerClass, projectName);
        addMethods(controllerClass, projectName);

        write(controllerPath, "Erro na escrita da classe Controller");
    }

    private void addControllerAnnotations(ClassOrInterfaceDeclaration controllerClass, String projectName) {
        controllerClass.addAnnotation("Controller");
        controllerClass.addSingleMemberAnnotation("RequestMapping", "\"/" + projectName + "\"");
    }

    private void addFields(ClassOrInterfaceDeclaration controllerClass, String projectName) {

        controllerClass.addPrivateField(nameUtils.getServiceClassName(projectName), nameUtils.getServiceVariableName(projectName)).addAnnotation("Autowired");
    }

    private void addMethods(ClassOrInterfaceDeclaration controllerClass, String projectName) {
        addFindMethod(controllerClass, projectName);
        addFindAllMethod(controllerClass, projectName);
        addCreateMethod(controllerClass, projectName);
        addUpdateMethod(controllerClass, projectName);
        addDeleteMethod(controllerClass, projectName);
    }

    private void addFindMethod(ClassOrInterfaceDeclaration controllerClass, String projectName) {

        String responseVariableName = nameUtils.getResponseVariableName(projectName);
        ClassOrInterfaceType optionalResponse = typeUtils.getClassOrInterfaceType(String.format("Optional<%s>", nameUtils.getResponseClassName(projectName)));

        VariableDeclarator responseDeclaration = new VariableDeclarator(
                optionalResponse,
                responseVariableName,
                new MethodCallExpr(new NameExpr(nameUtils.getServiceVariableName(projectName)), "find", argumentUtils.buildNameArgument("id")));

        MethodCallExpr getExpr = new MethodCallExpr(new NameExpr(responseVariableName), "get");
        MethodCallExpr isPresent = new MethodCallExpr(new NameExpr(responseVariableName), "isPresent");
        ReturnStmt returnOk = new ReturnStmt(new ObjectCreationExpr()
                .setType(typeUtils.getResponseEntityType(""))
                .setArguments(argumentUtils.buildStatusArgument(getExpr, "OK")));
        ReturnStmt returnNotFound = new ReturnStmt(new ObjectCreationExpr()
                .setType(typeUtils.getResponseEntityType(""))
                .setArguments(argumentUtils.buildEmptyStatusArgument("NOT_FOUND")));

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new VariableDeclarationExpr(responseDeclaration));
        blockStmt.addStatement(new IfStmt(isPresent, returnOk, returnNotFound));

        MethodDeclaration findMethod = controllerClass.addMethod("find", Modifier.Keyword.PUBLIC);
        findMethod.setType(typeUtils.getResponseEntityType(nameUtils.getResponseClassName(projectName)));
        findMethod.addSingleMemberAnnotation("GetMapping", "\"/{id}\"" );
        findMethod.addParameter(buildIdParameter());
        findMethod.setBody(blockStmt);
    }

    private void addFindAllMethod(ClassOrInterfaceDeclaration controllerClass, String projectName) {

        String responseListVariableName = nameUtils.getResponseVariableName(projectName) + "List";
        ClassOrInterfaceType responseList = typeUtils.getClassOrInterfaceType(String.format("List<%s>", nameUtils.getResponseClassName(projectName)));

        VariableDeclarator responseListDeclaration = new VariableDeclarator(
                responseList,
                responseListVariableName,
                new MethodCallExpr(new NameExpr(nameUtils.getServiceVariableName(projectName)), "findAll"));

        MethodCallExpr isEmpty = new MethodCallExpr(new NameExpr(responseListVariableName), "isEmpty");
        ReturnStmt returnOk = new ReturnStmt(new ObjectCreationExpr()
                .setType(typeUtils.getResponseEntityType(""))
                .setArguments(argumentUtils.buildStatusArgument(new NameExpr(responseListVariableName), "OK")));
        ReturnStmt returnNoContent = new ReturnStmt(new ObjectCreationExpr()
                .setType(typeUtils.getResponseEntityType(""))
                .setArguments(argumentUtils.buildEmptyStatusArgument("NO_CONTENT")));

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new VariableDeclarationExpr(responseListDeclaration));
        blockStmt.addStatement(new IfStmt(isEmpty, returnNoContent, returnOk));

        MethodDeclaration findAllMethod = controllerClass.addMethod("findAll", Modifier.Keyword.PUBLIC);
        findAllMethod.setType(typeUtils.getResponseEntityType(String.format("List<%s>", nameUtils.getResponseClassName(projectName))));
        findAllMethod.addAnnotation("GetMapping");
        findAllMethod.setBody(blockStmt);
    }

    private void addCreateMethod(ClassOrInterfaceDeclaration controllerClass, String projectName) {

        MethodCallExpr saveExpr = new MethodCallExpr(new NameExpr(nameUtils.getServiceVariableName(projectName)), "save", argumentUtils.buildNameArgument(nameUtils.getResourceVariableName(projectName)));
        ReturnStmt returnCreated = new ReturnStmt(new ObjectCreationExpr()
                .setType(typeUtils.getResponseEntityType(""))
                .setArguments(argumentUtils.buildStatusArgument(saveExpr, "CREATED")));

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(returnCreated);

        MethodDeclaration createMethod = controllerClass.addMethod("create", Modifier.Keyword.PUBLIC);
        createMethod.setType(typeUtils.getClassOrInterfaceType("ResponseEntity"));
        createMethod.addAnnotation("PostMapping");
        createMethod.addParameter(buildResourceParameter(projectName));
        createMethod.setBody(blockStmt);
    }

    private void addUpdateMethod(ClassOrInterfaceDeclaration controllerClass, String projectName) {

        MethodCallExpr saveExpr = new MethodCallExpr(new NameExpr(nameUtils.getServiceVariableName(projectName)), "save", argumentUtils.buildNameArgument(nameUtils.getResourceVariableName(projectName)));
        MethodCallExpr findExpr =  new MethodCallExpr(new NameExpr(nameUtils.getServiceVariableName(projectName)), "find", argumentUtils.buildNameArgument("id"));
        MethodCallExpr isPresent = new MethodCallExpr(findExpr, "isPresent");
        ReturnStmt returnOk = new ReturnStmt(new ObjectCreationExpr()
                .setType(typeUtils.getResponseEntityType(""))
                .setArguments(argumentUtils.buildStatusArgument(saveExpr, "OK")));
        ReturnStmt returnNotFound = new ReturnStmt(new ObjectCreationExpr()
                .setType(typeUtils.getResponseEntityType(""))
                .setArguments(argumentUtils.buildEmptyStatusArgument("NOT_FOUND")));

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new IfStmt(isPresent, returnOk, returnNotFound));

        MethodDeclaration updateMethod = controllerClass.addMethod("update", Modifier.Keyword.PUBLIC);
        updateMethod.setType(typeUtils.getClassOrInterfaceType("ResponseEntity"));
        updateMethod.addSingleMemberAnnotation("PutMapping", "\"/{id}\"" );
        updateMethod.addParameter(buildIdParameter());
        updateMethod.addParameter(buildResourceParameter(projectName));
        updateMethod.setBody(blockStmt);
    }

    private void addDeleteMethod(ClassOrInterfaceDeclaration controllerClass, String projectName) {

        MethodCallExpr deleteExpr = new MethodCallExpr(new NameExpr(nameUtils.getServiceVariableName(projectName)), "delete", argumentUtils.buildNameArgument("id"));
        ReturnStmt returnNoContent = new ReturnStmt(new ObjectCreationExpr()
                .setType(typeUtils.getResponseEntityType(""))
                .setArguments(argumentUtils.buildEmptyStatusArgument("NO_CONTENT")));

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(deleteExpr);
        blockStmt.addStatement(returnNoContent);

        MethodDeclaration deleteMethod = controllerClass.addMethod("delete", Modifier.Keyword.PUBLIC);
        deleteMethod.setType(typeUtils.getClassOrInterfaceType("ResponseEntity"));
        deleteMethod.addSingleMemberAnnotation("DeleteMapping", "\"/{id}\"" );
        deleteMethod.addParameter(buildIdParameter());
        deleteMethod.setBody(blockStmt);
    }

    private Parameter buildIdParameter() {
        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getStringType());
        parameter.setName("id");
        parameter.addAnnotation("PathVariable");
        return parameter;
    }

    private Parameter buildResourceParameter(String projectName) {
        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getClassOrInterfaceType(nameUtils.getResourceClassName(projectName)));
        parameter.setName(nameUtils.getResourceVariableName(projectName));
        parameter.addAnnotation("RequestBody");
        return parameter;
    }
}
