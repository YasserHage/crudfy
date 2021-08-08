package com.crudfy.services;

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

import java.io.FileWriter;
import java.io.IOException;

@Service
public class ServiceBuilder {

    @Autowired
    private NameUtils nameUtils;

    @Autowired
    private TypeUtils typeUtils;

    @Autowired
    private ArgumentUtils argumentUtils;

    public void buildMapper(String servicePath, String projectName) {

        CompilationUnit compilationUnit = new CompilationUnit();
        String interfaceName = nameUtils.getMapperClassName(projectName) ;

        //Interface and Package
        compilationUnit.setPackageDeclaration("com." + projectName + ".services");
        ClassOrInterfaceDeclaration mapperInterface = compilationUnit
                .addInterface(interfaceName)
                .setPublic(true);

        addMapperImports(compilationUnit, projectName);
        addMapperAnnotations(mapperInterface);
        addMapperMethods(mapperInterface, projectName);

        try {
            //File Writing
            FileWriter myWriter = new FileWriter(String.format("%s/%s.java", servicePath, interfaceName));
            myWriter.write(compilationUnit.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Erro na escrita da interface Mapper", e);
        }
    }

    public void buildService(String servicePath, String projectName) {

        CompilationUnit compilationUnit = new CompilationUnit();
        String className = nameUtils.getServiceClassName(projectName) ;

        //Interface and Package
        compilationUnit.setPackageDeclaration("com." + projectName + ".services");
        ClassOrInterfaceDeclaration serviceClass = compilationUnit
                .addClass(className)
                .setPublic(true);

        addServiceImports(compilationUnit, projectName);
        addServiceAnnotations(serviceClass);
        addFields(serviceClass, projectName);
        addServiceMethods(serviceClass, projectName);

        try {
            //File Writing
            FileWriter myWriter = new FileWriter(String.format("%s/%s.java", servicePath, className));
            myWriter.write(compilationUnit.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Erro na escrita da interface Mapper", e);
        }
    }

    private void addImportCommons(CompilationUnit compilationUnit, String projectName) {
        compilationUnit.addImport(nameUtils.getResponseImportPath(projectName));
        compilationUnit.addImport(nameUtils.getResourceImportPath(projectName));
        compilationUnit.addImport(nameUtils.getEntityImportPath(projectName));
        compilationUnit.addImport("java.util.List");
    }

    private void addMapperImports(CompilationUnit compilationUnit, String projectName) {
        addImportCommons(compilationUnit, projectName);
        compilationUnit.addImport("org.mapstruct.Mapper");
    }

    private void addServiceImports(CompilationUnit compilationUnit, String projectName) {
        addImportCommons(compilationUnit, projectName);
        compilationUnit.addImport(nameUtils.getRepositoryImportPath(projectName));
        compilationUnit.addImport("java.util.Optional");
        compilationUnit.addImport("java.util.ArrayList");
        compilationUnit.addImport("org.springframework.beans.factory.annotation.Autowired");
    }

    private void addMapperAnnotations(ClassOrInterfaceDeclaration mapperInterface) {
        mapperInterface.addAnnotation(new NormalAnnotationExpr().addPair("componentModel", "\"spring\"").setName("Mapper"));
    }

    private void addServiceAnnotations(ClassOrInterfaceDeclaration mapperInterface) {
        mapperInterface.addAnnotation("Service");
    }

    private void addFields(ClassOrInterfaceDeclaration serviceClass, String projectName) {

        serviceClass.addPrivateField(nameUtils.getRepositoryClassName(projectName), nameUtils.getRepositoryVariableName(projectName)).addAnnotation("Autowired");
        serviceClass.addPrivateField(nameUtils.getMapperClassName(projectName), nameUtils.getMapperVariableName(projectName)).addAnnotation("Autowired");
    }

    private void addMapperMethods(ClassOrInterfaceDeclaration mapperInterface, String projectName) {

        addToEntityMethod(mapperInterface, projectName);
        addToResponseMethod(mapperInterface, projectName);
        addToResponseListMethod(mapperInterface, projectName);
    }

    private void addToEntityMethod(ClassOrInterfaceDeclaration mapperInterface, String projectName) {

        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getClassOrInterfaceType(nameUtils.getResourceClassName(projectName)));
        parameter.setName(nameUtils.getResourceVariableName(projectName));

        MethodDeclaration toEntityMethod = mapperInterface.addMethod(nameUtils.toEntityMethod(projectName));
        toEntityMethod.setType(typeUtils.getClassOrInterfaceType(nameUtils.getBaseClassName(projectName)));
        toEntityMethod.addParameter(parameter);
        toEntityMethod.setBody(null);
    }

    private void addToResponseMethod(ClassOrInterfaceDeclaration mapperInterface, String projectName) {

        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getClassOrInterfaceType(nameUtils.getBaseClassName(projectName)));
        parameter.setName(projectName);

        MethodDeclaration toResponseMethod = mapperInterface.addMethod(nameUtils.toResponseMethod(projectName));
        toResponseMethod.setType(typeUtils.getClassOrInterfaceType(nameUtils.getResponseClassName(projectName)));
        toResponseMethod.addParameter(parameter);
        toResponseMethod.setBody(null);
    }

    private void addToResponseListMethod(ClassOrInterfaceDeclaration mapperInterface, String projectName) {

        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getClassOrInterfaceType(String.format("List<%s>", nameUtils.getBaseClassName(projectName))));
        parameter.setName(projectName + "List");

        MethodDeclaration toResponseListMethod = mapperInterface.addMethod(nameUtils.toResponseListMethod(projectName));
        toResponseListMethod.setType(typeUtils.getClassOrInterfaceType(String.format("List<%s>", nameUtils.getResponseClassName(projectName))));
        toResponseListMethod.addParameter(parameter);
        toResponseListMethod.setBody(null);
    }

    private void addServiceMethods(ClassOrInterfaceDeclaration serviceClass, String projectName) {

        addFindMethod(serviceClass, projectName);
        addFindAllMethod(serviceClass, projectName);
        addSaveMethod(serviceClass, projectName);
        addDeleteMethod(serviceClass, projectName);
    }

    private void addFindMethod(ClassOrInterfaceDeclaration serviceClass, String projectName) {

        ClassOrInterfaceType optionalResponse = typeUtils.getClassOrInterfaceType(String.format("Optional<%s>", nameUtils.getResponseClassName(projectName)));
        ClassOrInterfaceType optionalEntity = typeUtils.getClassOrInterfaceType(String.format("Optional<%s>", nameUtils.getBaseClassName(projectName)));

        VariableDeclarator entityDeclaration = new VariableDeclarator(
                optionalEntity,
                projectName,
                new MethodCallExpr(new NameExpr(nameUtils.getRepositoryVariableName(projectName)), "findById", argumentUtils.buildNameArgument("id")));

        MethodCallExpr getExpr = new MethodCallExpr(new NameExpr(projectName), "get");
        MethodCallExpr toResponse =  new MethodCallExpr(
                new NameExpr(nameUtils.getMapperVariableName(projectName)),
                nameUtils.toResponseMethod(projectName),
                argumentUtils.buildArguments(getExpr));
        MethodCallExpr isPresent = new MethodCallExpr(new NameExpr(projectName), "isPresent");
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

    private void addFindAllMethod(ClassOrInterfaceDeclaration serviceClass, String projectName) {

        String variableName = projectName + "List";

        VariableDeclarator listDeclaration = new VariableDeclarator(
                typeUtils.getClassOrInterfaceType(String.format("List<%s>", nameUtils.getBaseClassName(projectName))),
                variableName,
                new ObjectCreationExpr().setType(typeUtils.getClassOrInterfaceType("ArrayList")));

        MethodReferenceExpr addExpr = new MethodReferenceExpr().setScope(new NameExpr(variableName)).setIdentifier("add");
        MethodCallExpr findAllExpr = new MethodCallExpr(new NameExpr(nameUtils.getRepositoryVariableName(projectName)), "findAll");
        MethodCallExpr iterationFunction = new MethodCallExpr(findAllExpr, "forEach", argumentUtils.buildArguments(addExpr));

        MethodCallExpr toResponse = new MethodCallExpr(new NameExpr(nameUtils.getMapperVariableName(projectName)),
                nameUtils.toResponseListMethod(projectName),
                argumentUtils.buildNameArgument(variableName));

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new VariableDeclarationExpr(listDeclaration));
        blockStmt.addStatement(iterationFunction);
        blockStmt.addStatement(new ReturnStmt(toResponse));

        MethodDeclaration findAllMethod = serviceClass.addMethod("findAll", Modifier.Keyword.PUBLIC);
        findAllMethod.setType(typeUtils.getClassOrInterfaceType(String.format("List<%s>", nameUtils.getResponseClassName(projectName))));
        findAllMethod.setBody(blockStmt);
    }

    private void addSaveMethod(ClassOrInterfaceDeclaration serviceClass, String projectName) {

        MethodCallExpr toEntity = new MethodCallExpr(
                new NameExpr(nameUtils.getMapperVariableName(projectName)),
                nameUtils.toEntityMethod(projectName),
                argumentUtils.buildNameArgument(nameUtils.getResourceVariableName(projectName)));
        VariableDeclarator entityDeclaration = new VariableDeclarator(
                typeUtils.getClassOrInterfaceType(nameUtils.getBaseClassName(projectName)),
                projectName,
                toEntity);

        MethodCallExpr saveExpr = new MethodCallExpr(
                new NameExpr(nameUtils.getRepositoryVariableName(projectName)),
                "save",
                argumentUtils.buildNameArgument(projectName));
        MethodCallExpr toResponse = new MethodCallExpr(new NameExpr(nameUtils.getMapperVariableName(projectName)),
                nameUtils.toResponseMethod(projectName),
                argumentUtils.buildArguments(saveExpr));

        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new VariableDeclarationExpr(entityDeclaration));
        blockStmt.addStatement(new ReturnStmt(toResponse));

        MethodDeclaration saveMethod = serviceClass.addMethod("save", Modifier.Keyword.PUBLIC);
        saveMethod.setType(typeUtils.getClassOrInterfaceType(nameUtils.getResponseClassName(projectName)));
        saveMethod.addParameter(buildResourceParameter(projectName));
        saveMethod.setBody(blockStmt);
    }

    private void addDeleteMethod(ClassOrInterfaceDeclaration serviceClass, String projectName) {

        MethodCallExpr deleteExpr =  new MethodCallExpr(new NameExpr(nameUtils.getRepositoryVariableName(projectName)), "deleteById", argumentUtils.buildNameArgument("id"));

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

    private Parameter buildResourceParameter(String projectName) {
        Parameter parameter = new Parameter();
        parameter.setType(typeUtils.getClassOrInterfaceType(nameUtils.getResourceClassName(projectName)));
        parameter.setName(nameUtils.getResourceVariableName(projectName));
        return parameter;
    }
}
