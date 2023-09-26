package com.crudfy.services;

import com.crudfy.domains.exceptions.ResourceValidationException;
import com.crudfy.domains.resources.*;
import com.crudfy.services.builders.ControllerBuilder;
import com.crudfy.services.builders.DomainBuilder;
import com.crudfy.services.builders.RepositoryBuilder;
import com.crudfy.services.builders.ServiceBuilder;
import com.crudfy.services.utils.NameUtils;
import com.crudfy.services.utils.TypeUtils;
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
import com.github.javaparser.ast.type.VoidType;
import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.crudfy.domains.resources.Structure.DOMAIN;
import static com.crudfy.domains.resources.Structure.LAYER;

@Service
public class CrudService {

    @Autowired
    private DomainBuilder domainBuilder;

    @Autowired
    private ControllerBuilder controllerBuilder;

    @Autowired
    private RepositoryBuilder repositoryBuilder;

    @Autowired
    private ServiceBuilder serviceBuilder;

    @Autowired
    private NameUtils nameUtils;

    @Autowired
    private TypeUtils typeUtils;

    public void createProject(ComponentResource resource) {
        verifyResource(resource);
        String basePath = resource.getPath();
        String projectName = resource.getProjectName();
        Structure projectStructure = resource.getProjectStructure();

        if (LAYER.equals(projectStructure)) {
            createLayerStructurePackages(basePath, projectName, null);
        } else {
            createDomainStructurePackages(basePath, projectName, resource.getEntities());
        }
        createBaseProject(basePath, projectName, resource.getDatabase());

        for (Entity entity : resource.getEntities()) {
            String entityName = entity.getName();
            createDomainClasses(resource, entity);
            createRepositoryClasses(basePath, projectName, entityName, resource.getDatabase(), projectStructure);
            createControllerClasses(basePath, projectName, entityName, projectStructure);
            createServiceClasses(basePath, projectName, entityName, projectStructure);
        }
    }

    private void createLayerStructurePackages(String basePath, String projectName, String entityName) {
        new File(nameUtils.getControllerPath(basePath, projectName, entityName)).mkdirs();
        new File(nameUtils.getServicePath(basePath, projectName, entityName)).mkdir();
        new File(nameUtils.getDomainPath(basePath, projectName, entityName)).mkdir();
        new File(nameUtils.getRepositoryPath(basePath, projectName, entityName)).mkdir();
    }

    private void createDomainStructurePackages(String basePath, String projectName, List<Entity> entities) {
        entities.stream().map(Entity::getName).forEach(entityName -> {
            new File(nameUtils.getEntityRootPath(basePath, projectName, entityName)).mkdirs();
            createLayerStructurePackages(basePath, projectName, entityName);
        });
    }

    private void verifyResource(ComponentResource resource) {
        if (CollectionUtils.isEmpty(resource.getEntities())) {
            //TODO Translate to english
            throw new ResourceValidationException("É necessário ao menos uma entidade para construir o projeto");
        } else {
            for (Entity entity : resource.getEntities()) {
                List<Field> idList = entity.getFields().stream()
                        .filter(Field::isId)
                        .collect(Collectors.toList());
                if (idList.size() > 1) {
                    throw new ResourceValidationException("Não é possível criar entidades com chaves primárias compostas");
                }
            }
        }
    }

    private void createServiceClasses(String basePath, String projectName, String entityName, Structure projectStructure) {

        String servicePath = nameUtils.getServicePath(
                basePath, projectName, LAYER.equals(projectStructure) ? null : entityName);
        serviceBuilder.buildMapper(servicePath, projectName, entityName, projectStructure);
        serviceBuilder.buildService(servicePath, projectName, entityName, projectStructure);
    }

    private void createRepositoryClasses(String basePath, String projectName, String entityName, Database database, Structure projectStructure) {

        String repositoryPath = nameUtils.getRepositoryPath(
                basePath, projectName, LAYER.equals(projectStructure) ? null : entityName);
        repositoryBuilder.buildRepository(repositoryPath, projectName, entityName, database, projectStructure);
    }

    private void createDomainClasses(ComponentResource resource, Entity entity) {

        String entityName = entity.getName();
        List<Field> fields = entity.getFields();
        String projectName = resource.getProjectName();
        Structure projectStructure = resource.getProjectStructure();

        String domainPath = nameUtils.getDomainPath(
                resource.getPath(), projectName, LAYER.equals(resource.getProjectStructure()) ? null : entity.getName());
        domainBuilder.buildResponse(domainPath, projectName, entityName, fields, projectStructure);
        domainBuilder.buildResource(domainPath, projectName, entityName, fields, projectStructure);
        domainBuilder.buildEntity(domainPath, projectName, entityName, fields, resource.getDatabase(), projectStructure);
    }

    private void createControllerClasses(String basePath, String projectName, String entityName, Structure projectStructure) {

        String controllerPath = nameUtils.getControllerPath(basePath, projectName, LAYER.equals(projectStructure) ? null : entityName);
        controllerBuilder.buildController(controllerPath, projectName, entityName, projectStructure);
    }

    private void createBaseProject(String basePath, String projectName, Database database) {

        new File(nameUtils.getResourcePath(basePath)).mkdirs();
        new File(nameUtils.getTestRootPath(basePath, projectName)).mkdirs();

        createMainClass(basePath, projectName);
        createPomFile(basePath, projectName, database);
    }

    private void createMainClass(String basePath, String projectName) {

        CompilationUnit compilationUnit = new CompilationUnit();
        String className = nameUtils.getMainClassName(projectName);

        //Class and Package
        compilationUnit.setPackageDeclaration(nameUtils.getRootImportPath(projectName));
        ClassOrInterfaceDeclaration mainClass = compilationUnit.addClass(className).setPublic(true);

        //Imports
        compilationUnit.addImport("org.springframework.boot.SpringApplication");
        compilationUnit.addImport("org.springframework.boot.autoconfigure.SpringBootApplication");

        //Annotations
        mainClass.addAnnotation("SpringBootApplication");

        //Method Block
        BlockStmt blockStmt = new BlockStmt();
        Expression run = new MethodCallExpr(new NameExpr("SpringApplication"), "run", new NodeList<>(new ClassExpr(typeUtils.getClassOrInterfaceType(className)), new NameExpr("args")));
        blockStmt.addStatement(new ExpressionStmt(run));

        //Method
        MethodDeclaration getTest = mainClass.addMethod("main", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC).addParameter("String[]", "args");
        getTest.setType(new VoidType());
        getTest.setBody(blockStmt);

        try {
            //File Writing
            FileWriter myWriter = new FileWriter(String.format("%s/%s.java", nameUtils.getMainRootPath(basePath, projectName), className));
            myWriter.write(compilationUnit.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Erro na escrita da classe main", e);
        }
    }

    private void createPomFile(String basePath, String projectName, Database database) {

        Parent parent = new Parent();
        parent.setArtifactId("spring-boot-starter-parent");
        parent.setGroupId("org.springframework.boot");
        parent.setVersion("2.5.2");

        Properties properties = new Properties();
        properties.setProperty("java.version", "11");

        Build build = new Build();
        build.setPlugins(createPomPlugins());

        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setParent(parent);
        model.setGroupId( "some.group.id" );
        model.setArtifactId(projectName);
        model.setVersion("0.0.1-SNAPSHOT");
        model.setName(projectName);
        model.setDescription(projectName + " basic CRUD project (Made by CRUDFY)");
        model.setProperties(properties);
        model.setDependencies(createPomDependencies(database));
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

    private List<Plugin> createPomPlugins() {
        List<Plugin> plugins = new ArrayList<>();

        Plugin springMavenPlugin = new Plugin();
        springMavenPlugin.setGroupId("org.springframework.boot");
        springMavenPlugin.setArtifactId("spring-boot-maven-plugin");
        springMavenPlugin.setVersion("${project.parent.version}");
        plugins.add(springMavenPlugin);

        Xpp3Dom latestVersion = new Xpp3Dom("version");
        latestVersion.setValue("LATEST");

        Xpp3Dom lombokPathGroupId = new Xpp3Dom("groupId");
        lombokPathGroupId.setValue("org.projectlombok");

        Xpp3Dom lombokPathArtifactId = new Xpp3Dom("artifactId");
        lombokPathArtifactId.setValue("lombok");

        Xpp3Dom lombokPath = new Xpp3Dom("path");
        lombokPath.addChild(lombokPathGroupId);
        lombokPath.addChild(lombokPathArtifactId);
        lombokPath.addChild(latestVersion);

        Xpp3Dom mapstructPathGroupId = new Xpp3Dom("groupId");
        mapstructPathGroupId.setValue("org.mapstruct");

        Xpp3Dom mapstructPathArtifactId = new Xpp3Dom("artifactId");
        mapstructPathArtifactId.setValue("mapstruct-processor");

        Xpp3Dom mapstructPath = new Xpp3Dom("path");
        mapstructPath.addChild(mapstructPathGroupId);
        mapstructPath.addChild(mapstructPathArtifactId);
        mapstructPath.addChild(latestVersion);

        Xpp3Dom annotationProcessorPaths = new Xpp3Dom("annotationProcessorPaths");
        annotationProcessorPaths.addChild(lombokPath);
        annotationProcessorPaths.addChild(mapstructPath);

        Xpp3Dom source = new Xpp3Dom("source");
        source.setValue("11");

        Xpp3Dom target = new Xpp3Dom("target");
        target.setValue("11");

        Xpp3Dom mavenCompilerConfig = new Xpp3Dom("configuration");
        mavenCompilerConfig.addChild(source);
        mavenCompilerConfig.addChild(target);
        mavenCompilerConfig.addChild(annotationProcessorPaths);

        Plugin mavenCompilerPlugin = new Plugin();
        mavenCompilerPlugin.setGroupId("org.apache.maven.plugins");
        mavenCompilerPlugin.setArtifactId("maven-compiler-plugin");
        mavenCompilerPlugin.setVersion("3.8.1");
        mavenCompilerPlugin.setConfiguration(mavenCompilerConfig);
        plugins.add(mavenCompilerPlugin);

        return plugins;
    }

    private List<Dependency> createPomDependencies(Database database) {
        List<Dependency> dependencies = new ArrayList<>();

        switch (database) {
            case MONGODB:
                Dependency dataMongodb = new Dependency();
                dataMongodb.setGroupId("org.springframework.boot");
                dataMongodb.setArtifactId("spring-boot-starter-data-mongodb");
                dependencies.add(dataMongodb);
                break;
            case ELASTICSEARCH:
                Dependency dataElasticsearch = new Dependency();
                dataElasticsearch.setGroupId("org.springframework.data");
                dataElasticsearch.setArtifactId("spring-data-elasticsearch");
                dependencies.add(dataElasticsearch);
                break;
            default:
                Dependency dataJpa = new Dependency();
                dataJpa.setGroupId("org.springframework.boot");
                dataJpa.setArtifactId("spring-boot-starter-data-jpa");
                dependencies.add(dataJpa);

                Dependency dataJdbc = new Dependency();
                dataJdbc.setGroupId("org.springframework.boot");
                dataJdbc.setArtifactId("spring-boot-starter-data-jdbc");
                dependencies.add(dataJdbc);

                Dependency mysql = new Dependency();
                mysql.setGroupId("mysql");
                mysql.setArtifactId("mysql-connector-java");
                mysql.setScope("runtime");
                dependencies.add(mysql);
                break;
        }

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

        Dependency mapstruct = new Dependency();
        mapstruct.setGroupId("org.mapstruct");
        mapstruct.setArtifactId("mapstruct");
        mapstruct.setVersion("LATEST");
        dependencies.add(mapstruct);

        Dependency springTest = new Dependency();
        springTest.setGroupId("org.springframework.boot");
        springTest.setArtifactId("spring-boot-starter-test");
        springTest.setScope("test");
        dependencies.add(springTest);

        return dependencies;
    }
}
