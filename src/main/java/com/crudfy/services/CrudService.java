package com.crudfy.services;

import com.crudfy.domains.ComponentResource;
import com.crudfy.domains.Field;
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
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class CrudService {

    @Autowired
    private DomainBuilder domainBuilder;

    public void createProject(ComponentResource resource) {
        String rootPath = String.format("%s/src/main/java/com/%s", resource.getPath(), resource.getName());
        String projectName = resource.getName();

        createBaseProject(projectName, resource.getPath(), rootPath);
        createDomainClasses(projectName, rootPath + "/domains", resource.getFields());
    }

    private void createDomainClasses(String projectName, String domainPath, List<Field> fields) {
        domainBuilder.buildResponse(projectName, domainPath, fields);
        domainBuilder.buildResource(projectName, domainPath, fields);
        domainBuilder.buildEntity(projectName, domainPath, fields);
    }

    private void createBaseProject(String projectName, String basePath, String rootPath) {

        String resourcesPath = String.format("%s/src/main/resources", basePath);

        new File(resourcesPath).mkdirs();
        new File(rootPath + "/controllers").mkdirs();
        new File(rootPath + "/services").mkdir();
        new File(rootPath + "/domains").mkdir();
        new File(rootPath + "/repositories").mkdir();
        new File(String.format("%s/src/test/java/com/%s", basePath, projectName)).mkdirs();

        createMainClass(projectName, rootPath);
        createPomFile(projectName, basePath);
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

        Parent parent = new Parent();
        parent.setArtifactId("spring-boot-starter-parent");
        parent.setGroupId("org.springframework.boot");
        parent.setVersion("2.5.2");

        Properties properties = new Properties();
        properties.setProperty("java.version", "11");

        List<Dependency> dependencies = new ArrayList<>();

        Dependency springWeb = new Dependency();
        springWeb.setGroupId("org.springframework.boot");
        springWeb.setArtifactId("spring-boot-starter-web");
        dependencies.add(springWeb);

        Dependency springStarter = new Dependency();
        springStarter.setGroupId("org.springframework.boot");
        springStarter.setArtifactId("spring-boot-starter");
        dependencies.add(springStarter);

        Dependency lombok = new Dependency();
        lombok.setGroupId("org.projectlombok");
        lombok.setArtifactId("lombok");
        dependencies.add(lombok);

        Dependency springTest = new Dependency();
        springTest.setGroupId("org.springframework.boot");
        springTest.setArtifactId("spring-boot-starter-test");
        springTest.setScope("test");
        dependencies.add(springTest);

        Plugin plugin = new Plugin();
        plugin.setGroupId("org.springframework.boot");
        plugin.setArtifactId("spring-boot-maven-plugin");
        plugin.setVersion("${project.parent.version}");

        List<Plugin> plugins = new ArrayList<>();
        plugins.add(plugin);

        Build build = new Build();
        build.setPlugins(plugins);

        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setParent(parent);
        model.setGroupId( "some.group.id" );
        model.setArtifactId(projectName);
        model.setVersion("0.0.1-SNAPSHOT");
        model.setName(projectName);
        model.setDescription(projectName + " basic CRUD project (Made by CRUDFY)");
        model.setProperties(properties);
        model.setDependencies(dependencies);
        model.setBuild(build);

        try {
            File file = new File(basePath + "\\pom.xml");
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            new MavenXpp3Writer().write( writer, model );
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Problema ao criar o arquivo pom.xml", e);
        }
    }
}
