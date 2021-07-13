package com.crudfy.services;

import com.crudfy.domains.ComponentResource;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class CrudService {

    public void createProject(ComponentResource resource) {
        createBaseProject(resource.getName(), resource.getPath());


    }

    private void createBaseProject(String projectName, String basePath) {

        String rootPath = String.format("%s/src/main/java/com/%s", basePath, projectName);

        new File(rootPath + "/controllers").mkdirs();
        new File(rootPath + "/services").mkdir();
        new File(rootPath + "/domains").mkdir();
        new File(rootPath + "/repositories").mkdir();
        new File(String.format("%s/src/main/resources/", basePath, projectName)).mkdirs();
        new File(String.format("%s/src/test/java/com/%s", basePath, projectName)).mkdirs();


        createMainClass(projectName, rootPath);
        createPomFile(projectName, basePath);
        // Fazer application.properties
    }

    private void createMainClass(String projectName, String rootPath) {

        JavaParser parser = new JavaParser();
        CompilationUnit compilationUnit = new CompilationUnit();
        String className = projectName.substring(0, 1).toUpperCase() + projectName.substring(1) + "Application";

        //Types
        Type voidType = parser.parseType("void").getResult().get();
        ClassOrInterfaceType mainType = parser.parseClassOrInterfaceType(className).getResult().get();

        //Class and Package
        compilationUnit.setPackageDeclaration("com." + projectName);
        ClassOrInterfaceDeclaration mainClass = compilationUnit.addClass(className).setPublic(true);

        //Imports
        compilationUnit.addImport("org.springframework.boot.SpringApplication");
        compilationUnit.addImport("org.springframework.boot.autoconfigure.SpringBootApplication");

        //Annotations
        mainClass.addAnnotation("SpringBootApplication");

        //Method Block
        BlockStmt blockStmt = new BlockStmt();
        Expression run = new MethodCallExpr(new NameExpr("SpringApplication"), "run", new NodeList<>(new ClassExpr(mainType), new NameExpr("args")));
        blockStmt.addStatement(new ExpressionStmt(run));

        //Method
        MethodDeclaration getTest = mainClass.addMethod("main", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC).addParameter("String[]", "args");
        getTest.setType(voidType);
        getTest.setBody(blockStmt);

        try {
            //File Writing
            FileWriter myWriter = new FileWriter(String.format("%s/%s.java", rootPath, className));
            myWriter.write(compilationUnit.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Erro na escrita da classe main", e);
        }
    }

    private void createPomFile(String projectName, String basePath) {

    }
}
