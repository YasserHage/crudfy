package com.crudfy.services;

import com.crudfy.services.utils.NameUtils;
import com.crudfy.services.utils.TypeUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.VoidType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;

@Service
public class ControllerBuilder {

    @Autowired
    private NameUtils nameUtils;

    @Autowired
    private TypeUtils typeUtils;

    public void buildController(String controllerPath, String projectName) {

        CompilationUnit compilationUnit = new CompilationUnit();
        String className = nameUtils.getControllerClassName(projectName) ;

        //Class and Package
        compilationUnit.setPackageDeclaration("com." + projectName + ".controllers");
        ClassOrInterfaceDeclaration controllerClass = compilationUnit.addClass(className).setPublic(true);

        addImports(compilationUnit, projectName);
        addAnnotations(controllerClass, projectName);
        addMethods(controllerClass, projectName);

        try {
            //File Writing
            FileWriter myWriter = new FileWriter(String.format("%s/%s.java", controllerPath, className));
            myWriter.write(compilationUnit.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Erro na escrita da classe Controller", e);
        }
    }

    private void addImports(CompilationUnit compilationUnit, String projectName) {
        compilationUnit.addImport(nameUtils.getResourceImportPath(projectName));
        compilationUnit.addImport(nameUtils.getResponseImportPath(projectName));
        compilationUnit.addImport("org.springframework.http.ResponseEntity");
        compilationUnit.addImport("org.springframework.stereotype.Controller");
        compilationUnit.addImport("org.springframework.web.bind.annotation.*");
        compilationUnit.addImport("java.util.List");
    }

    private void addAnnotations(ClassOrInterfaceDeclaration controllerClass, String projectName) {
        controllerClass.addAnnotation("Controller");
        controllerClass.addSingleMemberAnnotation("RequestMapping", "\"/" + projectName + "\"");
    }

    private void addMethods(ClassOrInterfaceDeclaration controllerClass, String projectName) {
        addFindMethod(controllerClass, projectName);
        addFindAllMethod(controllerClass, projectName);
        addCreateMethod(controllerClass, projectName);
        addUpdateMethod(controllerClass, projectName);
        addDeleteMethod(controllerClass);
    }

    private void addFindMethod(ClassOrInterfaceDeclaration controllerClass, String projectName) {

        MethodDeclaration findMethod = controllerClass.addMethod("find", Modifier.Keyword.PUBLIC);
        findMethod.setType(typeUtils.getResponseEntityType(nameUtils.getResponseClassName(projectName)));
        findMethod.addSingleMemberAnnotation("GetMapping", "\"/{id}\"" );
        findMethod.addParameter(buildIdParameter());
    }

    private void addFindAllMethod(ClassOrInterfaceDeclaration controllerClass, String projectName) {

        MethodDeclaration findAllMethod = controllerClass.addMethod("findAll", Modifier.Keyword.PUBLIC);
        findAllMethod.setType(typeUtils.getResponseEntityType(String.format("List<%s>", nameUtils.getResponseClassName(projectName))));
        findAllMethod.addAnnotation("GetMapping");
    }

    private void addCreateMethod(ClassOrInterfaceDeclaration controllerClass, String projectName) {

        MethodDeclaration createMethod = controllerClass.addMethod("create", Modifier.Keyword.PUBLIC);
        createMethod.setType(typeUtils.getClassOrInterfaceType("ResponseEntity"));
        createMethod.addAnnotation("PostMapping");
        createMethod.addParameter(buildResourceParameter(projectName));
    }

    private void addUpdateMethod(ClassOrInterfaceDeclaration controllerClass, String projectName) {

        MethodDeclaration updateMethod = controllerClass.addMethod("update", Modifier.Keyword.PUBLIC);
        updateMethod.setType(typeUtils.getClassOrInterfaceType("ResponseEntity"));
        updateMethod.addSingleMemberAnnotation("PutMapping", "\"/{id}\"" );
        updateMethod.addParameter(buildIdParameter());
        updateMethod.addParameter(buildResourceParameter(projectName));
    }

    private void addDeleteMethod(ClassOrInterfaceDeclaration controllerClass) {

        MethodDeclaration deleteMethod = controllerClass.addMethod("delete", Modifier.Keyword.PUBLIC);
        deleteMethod.setType(new VoidType());
        deleteMethod.addSingleMemberAnnotation("DeleteMapping", "\"/{id}\"" );
        deleteMethod.addParameter(buildIdParameter());
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
        return parameter;
    }
}
