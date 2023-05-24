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
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.VoidType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ServiceBuilder extends ClassOrInterfaceBuilder{

    @Autowired
    private NameUtils nameUtils;

    @Autowired
    private TypeUtils typeUtils;

    @Autowired
    private ArgumentUtils argumentUtils;

    public void buildMapper(String servicePath, String projectName, String entityName) {

        String interfaceName = nameUtils.getMapperClassName(entityName) ;

        CompilationUnit compilationUnit = initialize(nameUtils.getRootImportPath(projectName) + ".services", interfaceName, true);
        ClassOrInterfaceDeclaration mapperInterface = compilationUnit.getInterfaceByName(interfaceName).get();

        addImports(Arrays.asList(
                nameUtils.getResponseImportPath(projectName, entityName),
                nameUtils.getResourceImportPath(projectName, entityName),
                nameUtils.getEntityImportPath(projectName, entityName),
                "java.util.List",
                "org.mapstruct.Mapper"));
        addMapperAnnotations(mapperInterface);
        addMapperMethods(mapperInterface, entityName);

        write(servicePath, "Erro na escrita da interface Mapper");
    }

    public void buildService(String servicePath, String projectName, String entityName) {

        String className = nameUtils.getServiceClassName(entityName) ;

        CompilationUnit compilationUnit = initialize(nameUtils.getRootImportPath(projectName) + ".services", className, false);
        ClassOrInterfaceDeclaration serviceClass = compilationUnit.getClassByName(className).get();

        addImports(Arrays.asList(
                nameUtils.getEntityImportPath(projectName, entityName),
                nameUtils.getResourceImportPath(projectName, entityName),
                nameUtils.getResponseImportPath(projectName, entityName),
                nameUtils.getRepositoryImportPath(projectName, entityName),
                "java.util.List",
                "java.util.Optional",
                "java.util.ArrayList",
                "org.springframework.stereotype.Service",
                "org.springframework.beans.factory.annotation.Autowired"
        ));
        addAnnotation("Service");
        addFields(serviceClass, entityName);
        addServiceMethods(serviceClass, entityName);

        write(servicePath, "Erro na escrita da classe Service");
    }

    private void addMapperAnnotations(ClassOrInterfaceDeclaration mapperInterface) {
        mapperInterface.addAnnotation(new NormalAnnotationExpr().addPair("componentModel", "\"spring\"").setName("Mapper"));
    }

    private void addFields(ClassOrInterfaceDeclaration serviceClass, String entityName) {

        serviceClass.addPrivateField(nameUtils.getRepositoryClassName(entityName), nameUtils.getRepositoryVariableName(entityName)).addAnnotation("Autowired");
        serviceClass.addPrivateField(nameUtils.getMapperClassName(entityName), nameUtils.getMapperVariableName(entityName)).addAnnotation("Autowired");
    }

    private void addMapperMethods(ClassOrInterfaceDeclaration mapperInterface, String entityName) {

        addToEntityMethod(mapperInterface, entityName);
        addToResponseMethod(mapperInterface, entityName);
        addToResponseListMethod(mapperInterface, entityName);
    }

    private void addToEntityMethod(ClassOrInterfaceDeclaration mapperInterface, String entityName) {

        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getClassOrInterfaceType(nameUtils.getResourceClassName(entityName)));
        parameter.setName(nameUtils.getResourceVariableName(entityName));

        MethodDeclaration toEntityMethod = mapperInterface.addMethod(nameUtils.toEntityMethod(entityName));
        toEntityMethod.setType(typeUtils.getClassOrInterfaceType(nameUtils.getBaseClassName(entityName)));
        toEntityMethod.addParameter(parameter);
        toEntityMethod.setBody(null);
    }

    private void addToResponseMethod(ClassOrInterfaceDeclaration mapperInterface, String entityName) {

        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getClassOrInterfaceType(nameUtils.getBaseClassName(entityName)));
        parameter.setName(entityName);

        MethodDeclaration toResponseMethod = mapperInterface.addMethod(nameUtils.toResponseMethod(entityName));
        toResponseMethod.setType(typeUtils.getClassOrInterfaceType(nameUtils.getResponseClassName(entityName)));
        toResponseMethod.addParameter(parameter);
        toResponseMethod.setBody(null);
    }

    private void addToResponseListMethod(ClassOrInterfaceDeclaration mapperInterface, String entityName) {

        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getClassOrInterfaceType(String.format("List<%s>", nameUtils.getBaseClassName(entityName))));
        parameter.setName(entityName + "List");

        MethodDeclaration toResponseListMethod = mapperInterface.addMethod(nameUtils.toResponseListMethod(entityName));
        toResponseListMethod.setType(typeUtils.getClassOrInterfaceType(String.format("List<%s>", nameUtils.getResponseClassName(entityName))));
        toResponseListMethod.addParameter(parameter);
        toResponseListMethod.setBody(null);
    }

    private void addServiceMethods(ClassOrInterfaceDeclaration serviceClass, String entityName) {

        addFindMethod(serviceClass, entityName);
        addFindAllMethod(serviceClass, entityName);
        addSaveMethod(serviceClass, entityName);
        addDeleteMethod(serviceClass, entityName);
    }

    private void addFindMethod(ClassOrInterfaceDeclaration serviceClass, String entityName) {

        ClassOrInterfaceType optionalResponse = typeUtils.getClassOrInterfaceType(String.format("Optional<%s>", nameUtils.getResponseClassName(entityName)));
        ClassOrInterfaceType optionalEntity = typeUtils.getClassOrInterfaceType(String.format("Optional<%s>", nameUtils.getBaseClassName(entityName)));

        VariableDeclarator entityDeclaration = new VariableDeclarator(
                optionalEntity,
                entityName,
                new MethodCallExpr(new NameExpr(nameUtils.getRepositoryVariableName(entityName)), "findById", argumentUtils.buildNameArgument("id")));

        MethodCallExpr getExpr = new MethodCallExpr(new NameExpr(entityName), "get");
        MethodCallExpr toResponse =  new MethodCallExpr(
                new NameExpr(nameUtils.getMapperVariableName(entityName)),
                nameUtils.toResponseMethod(entityName),
                argumentUtils.buildArguments(getExpr));
        MethodCallExpr isPresent = new MethodCallExpr(new NameExpr(entityName), "isPresent");
        MethodCallExpr toOptional = new MethodCallExpr(new NameExpr("Optional"),"of", argumentUtils.buildArguments(toResponse));
        MethodCallExpr empty = new MethodCallExpr(new NameExpr("Optional"), "empty");

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new VariableDeclarationExpr(entityDeclaration));
        blockStmt.addStatement(new ReturnStmt(new ConditionalExpr(isPresent, toOptional, empty)));

        MethodDeclaration findMethod = serviceClass.addMethod("find", Modifier.Keyword.PUBLIC);
        findMethod.setType(optionalResponse);
        findMethod.addParameter(buildIdParameter());
        findMethod.setBody(blockStmt);
    }

    private void addFindAllMethod(ClassOrInterfaceDeclaration serviceClass, String entityName) {

        String variableName = entityName + "List";

        VariableDeclarator listDeclaration = new VariableDeclarator(
                typeUtils.getClassOrInterfaceType(String.format("List<%s>", nameUtils.getBaseClassName(entityName))),
                variableName,
                new ObjectCreationExpr().setType(typeUtils.getClassOrInterfaceType("ArrayList")));

        MethodReferenceExpr addExpr = new MethodReferenceExpr().setScope(new NameExpr(variableName)).setIdentifier("add");
        MethodCallExpr findAllExpr = new MethodCallExpr(new NameExpr(nameUtils.getRepositoryVariableName(entityName)), "findAll");
        MethodCallExpr iterationFunction = new MethodCallExpr(findAllExpr, "forEach", argumentUtils.buildArguments(addExpr));

        MethodCallExpr toResponse = new MethodCallExpr(new NameExpr(nameUtils.getMapperVariableName(entityName)),
                nameUtils.toResponseListMethod(entityName),
                argumentUtils.buildNameArgument(variableName));

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new VariableDeclarationExpr(listDeclaration));
        blockStmt.addStatement(iterationFunction);
        blockStmt.addStatement(new ReturnStmt(toResponse));

        MethodDeclaration findAllMethod = serviceClass.addMethod("findAll", Modifier.Keyword.PUBLIC);
        findAllMethod.setType(typeUtils.getClassOrInterfaceType(String.format("List<%s>", nameUtils.getResponseClassName(entityName))));
        findAllMethod.setBody(blockStmt);
    }

    private void addSaveMethod(ClassOrInterfaceDeclaration serviceClass, String entityName) {

        MethodCallExpr toEntity = new MethodCallExpr(
                new NameExpr(nameUtils.getMapperVariableName(entityName)),
                nameUtils.toEntityMethod(entityName),
                argumentUtils.buildNameArgument(nameUtils.getResourceVariableName(entityName)));
        VariableDeclarator entityDeclaration = new VariableDeclarator(
                typeUtils.getClassOrInterfaceType(nameUtils.getBaseClassName(entityName)),
                entityName,
                toEntity);

        MethodCallExpr saveExpr = new MethodCallExpr(
                new NameExpr(nameUtils.getRepositoryVariableName(entityName)),
                "save",
                argumentUtils.buildNameArgument(entityName));
        MethodCallExpr toResponse = new MethodCallExpr(new NameExpr(nameUtils.getMapperVariableName(entityName)),
                nameUtils.toResponseMethod(entityName),
                argumentUtils.buildArguments(saveExpr));

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new VariableDeclarationExpr(entityDeclaration));
        blockStmt.addStatement(new ReturnStmt(toResponse));

        MethodDeclaration saveMethod = serviceClass.addMethod("save", Modifier.Keyword.PUBLIC);
        saveMethod.setType(typeUtils.getClassOrInterfaceType(nameUtils.getResponseClassName(entityName)));
        saveMethod.addParameter(buildResourceParameter(entityName));
        saveMethod.setBody(blockStmt);
    }

    private void addDeleteMethod(ClassOrInterfaceDeclaration serviceClass, String entityName) {

        MethodCallExpr deleteExpr =  new MethodCallExpr(new NameExpr(nameUtils.getRepositoryVariableName(entityName)), "deleteById", argumentUtils.buildNameArgument("id"));

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(deleteExpr);

        MethodDeclaration deleteMethod = serviceClass.addMethod("delete", Modifier.Keyword.PUBLIC);
        deleteMethod.setType(new VoidType());
        deleteMethod.addParameter(buildIdParameter());
        deleteMethod.setBody(blockStmt);
    }

    private Parameter buildIdParameter() {
        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getStringType());
        parameter.setName("id");
        return parameter;
    }

    private Parameter buildResourceParameter(String entityName) {
        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getClassOrInterfaceType(nameUtils.getResourceClassName(entityName)));
        parameter.setName(nameUtils.getResourceVariableName(entityName));
        return parameter;
    }
}
