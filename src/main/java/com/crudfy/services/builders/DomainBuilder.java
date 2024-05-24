package com.crudfy.services.builders;

import com.crudfy.domains.resources.Database;
import com.crudfy.domains.resources.DomainType;
import com.crudfy.domains.resources.Field;
import com.crudfy.domains.resources.Structure;
import com.crudfy.services.utils.ImportsMapper;
import com.crudfy.services.utils.NameUtils;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

        buildDomainClass(responseClass, fields, DomainType.RESPONSE, projectName, projectStructure);

        write(domainPath, "Erro na escrita da classe Response");
    }

    public void buildResource(String domainPath, String projectName, String entityName, List<Field> fields, Structure projectStructure) {

        String className = nameUtils.getResourceClassName(entityName);
        CompilationUnit compilationUnit = initialize(nameUtils.getDomainImportPath(projectName, entityName, projectStructure), className, false);
        ClassOrInterfaceDeclaration resourceClass = compilationUnit.getClassByName(className).get();

        buildDomainClass(resourceClass, fields, DomainType.RESOURCE, projectName, projectStructure);

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
        buildDomainClass(entityClass, fields, DomainType.ENTITY, projectName, projectStructure);
        addId(entityClass, fields, database);

        write(domainPath, "Erro na escrita da classe Entity");
    }

    private void buildDomainClass(ClassOrInterfaceDeclaration domainClass, List<Field> fields, DomainType domainType, String projectName, Structure projectStructure) {
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
        addFields(domainClass, fields, domainType, projectName, projectStructure);
    }

    private void addFields(ClassOrInterfaceDeclaration commonClass, List<Field> fields, DomainType domainType, String projectName, Structure projectStructure) {
        List<String> imports = new ArrayList<>();
        fields.forEach(field -> addField(commonClass, field, imports, domainType, projectName, projectStructure));
        addImports(imports.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()));
    }

    private void addField(ClassOrInterfaceDeclaration commonClass, Field field, List<String> imports, DomainType domainType, String projectName, Structure projectStructure) {
        String fieldType = field.getType();
        if (field.isSubEntity()) {
            if (fieldType.contains(">")) {
                Matcher matcher= Pattern.compile("<(.*?)>").matcher(fieldType);
                if(matcher.find()) {
                    String realType = matcher.group(1);
                    fieldType = fieldType.replace(realType, findSubEntityType(realType, domainType));
                    imports.add(findSubEntityImport(realType, domainType, projectName, projectStructure));
                }
            } else {
                fieldType = findSubEntityType(field.getType(), domainType);
                imports.add(findSubEntityImport(field.getType(), domainType, projectName, projectStructure));
            }
        }

        createField(commonClass, field.getName(), fieldType);
        imports.addAll(findFieldImports(field));
    }

    private void createField(ClassOrInterfaceDeclaration commonClass, String name, String type) {
        JavaParser parser = new JavaParser();
        Type fieldType = parser.parseType(type).getResult().get();
        commonClass.addField(fieldType, name, Modifier.Keyword.PRIVATE);
    }

    private List<String> findFieldImports(Field field) {
        List<String> imports = mapper.getImports(field.getType());
        return  imports == null ? new ArrayList<>() : imports;
    }

    private String findSubEntityImport(String type, DomainType domainType, String projectName, Structure projectStructure) {
        switch (domainType) {
            case RESPONSE:
                return nameUtils.getResponseImportPath(projectName, type, projectStructure);
            case RESOURCE:
                return nameUtils.getResourceImportPath(projectName, type, projectStructure);
            default:
                return nameUtils.getEntityImportPath(projectName, type, projectStructure);
        }
    }

    private String findSubEntityType(String type, DomainType domainType) {
        switch (domainType) {
            case RESPONSE:
                return nameUtils.getResponseClassName(type);
            case RESOURCE:
                return nameUtils.getResourceClassName(type);
            default:
                return type;
        }
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
