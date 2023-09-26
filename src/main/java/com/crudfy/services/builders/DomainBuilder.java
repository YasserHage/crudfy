package com.crudfy.services.builders;

import com.crudfy.domains.resources.Database;
import com.crudfy.domains.resources.Field;
import com.crudfy.domains.resources.Structure;
import com.crudfy.services.utils.ImportsMapper;
import com.crudfy.services.utils.NameUtils;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DomainBuilder extends ClassOrInterfaceBuilder{

    @Autowired
    private ImportsMapper mapper;

    @Autowired
    private NameUtils nameUtils;

    public void buildResponse(String domainPath, String projectName, String entityName, List<Field> fields, Structure projectStructure) {

        String className = nameUtils.getResponseClassName(entityName) ;
        CompilationUnit compilationUnit = initialize(nameUtils.getDomainImportPath(projectName, entityName, projectStructure), className, false);
        ClassOrInterfaceDeclaration responseClass = compilationUnit.getClassByName(className).get();

        buildDomainClass(responseClass, fields);

        write(domainPath, "Erro na escrita da classe Response");
    }

    public void buildResource(String domainPath, String projectName, String entityName, List<Field> fields, Structure projectStructure) {

        String className = nameUtils.getResourceClassName(entityName);
        CompilationUnit compilationUnit = initialize(nameUtils.getDomainImportPath(projectName, entityName, projectStructure), className, false);
        ClassOrInterfaceDeclaration resourceClass = compilationUnit.getClassByName(className).get();

        buildDomainClass(resourceClass, fields);

        write(domainPath, "Erro na escrita da classe Resource");
    }

    public void buildEntity(String domainPath, String projectName, String entityName, List<Field> fields, Database database, Structure projectStructure) {

        String className = nameUtils.getBaseClassName(entityName);
        CompilationUnit compilationUnit = initialize(nameUtils.getDomainImportPath(projectName, entityName, projectStructure), className, false);
        ClassOrInterfaceDeclaration entityClass = compilationUnit.getClassByName(className).get();

        switch (database) {
            case MONGODB:
                addImports(Arrays.asList(
                        "org.springframework.data.mongodb.core.mapping.Document"
                ));
                addAnnotation("Document", Map.of("value", "\"" + entityName.toLowerCase() + "\""));
                break;
            case ELASTICSEARCH:
                addImports(Arrays.asList(
                        "org.springframework.data.elasticsearch.annotations.Document"
                ));
                addAnnotation("Document", Map.of("indexName", "\"" + entityName.toLowerCase() + "\""));
                break;
            default:
                addImports(Arrays.asList(
                        "javax.persistence.Entity"
                ));
                addAnnotation("Entity");
                break;
        }
        buildDomainClass(entityClass, fields);
        addId(entityClass, fields, database);

        write(domainPath, "Erro na escrita da classe Entity");
    }

    private void buildDomainClass(ClassOrInterfaceDeclaration domainClass, List<Field> fields) {
        addImports(Arrays.asList(
                "lombok.Data",
                "lombok.AllArgsConstructor",
                "lombok.NoArgsConstructor"
        ));
        addAnnotations(Arrays.asList(
                "Data",
                "AllArgsConstructor",
                "NoArgsConstructor"
        ));
        addFields(domainClass, fields);
    }

    private void addFields(ClassOrInterfaceDeclaration commonClass, List<Field> fields) {
        JavaParser parser = new JavaParser();
        List<String> imports = new ArrayList<>();
        fields.forEach(field -> {
            Type fieldType = parser.parseType(field.getType()).getResult().get();
            commonClass.addField(fieldType, field.getName(), Modifier.Keyword.PRIVATE);
            List<String> fieldImports = mapper.getImports(field.getType());
            if (Objects.nonNull(fieldImports)) {
                imports.addAll(fieldImports);
            }
        });
        addImports(imports.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()));
    }

    private void addId(ClassOrInterfaceDeclaration entityClass, List<Field> fields, Database database) {

        Optional<Field> id = fields.stream().filter(Field::isId).findFirst();

        if (id.isPresent()) {
            if (database.equals(Database.MONGODB) || database.equals(Database.ELASTICSEARCH))  {
                addImports(Arrays.asList(
                        "org.springframework.data.annotation.Id"
                ));
            } else {
                addImports(Arrays.asList(
                        "javax.persistence.Id"
                ));
            }
            entityClass.getFieldByName(id.get().getName()).get().addAnnotation("Id");
        }
    }
}
