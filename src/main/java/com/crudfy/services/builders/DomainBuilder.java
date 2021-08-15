package com.crudfy.services.builders;

import com.crudfy.domains.resources.Field;
import com.crudfy.services.utils.ImportsMapper;
import com.crudfy.services.utils.NameUtils;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DomainBuilder extends ClassOrInterfaceBuilder{

    @Autowired
    private ImportsMapper mapper;

    @Autowired
    private NameUtils nameUtils;

    public void buildResponse(String domainPath, String projectName, List<Field> fields) {

        String className = nameUtils.getResponseClassName(projectName) ;
        CompilationUnit compilationUnit = initialize("com." + projectName + ".domains", className, false);
        ClassOrInterfaceDeclaration responseClass = compilationUnit.getClassByName(className).get();

        buildDomainClass(responseClass, fields);

        write(domainPath, "Erro na escrita da classe Response");
    }

    public void buildResource(String domainPath, String projectName, List<Field> fields) {

        String className = nameUtils.getResourceClassName(projectName);
        CompilationUnit compilationUnit = initialize("com." + projectName + ".domains", className, false);
        ClassOrInterfaceDeclaration resourceClass = compilationUnit.getClassByName(className).get();

        buildDomainClass(resourceClass, fields);

        write(domainPath, "Erro na escrita da classe Resource");
    }

    public void buildEntity(String domainPath, String projectName, List<Field> fields) {

        String className = nameUtils.getBaseClassName(projectName);
        CompilationUnit compilationUnit = initialize("com." + projectName + ".domains", className, false);
        ClassOrInterfaceDeclaration entityClass = compilationUnit.getClassByName(className).get();

        addImports(Arrays.asList(
                "javax.persistence.Entity"
        ));
        addAnnotations(Arrays.asList(
                "Entity"
        ));
        buildDomainClass(entityClass, fields);
        addId(entityClass, fields);

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
            imports.addAll(mapper.getImports(field.getType()));
        });
        addImports(imports.stream().distinct().collect(Collectors.toList()));
    }

    private void addId(ClassOrInterfaceDeclaration entityClass, List<Field> fields) {

        Optional<Field> id = fields.stream().filter(Field::isId).findFirst();

        if (id.isPresent()) {
            addImports(Arrays.asList(
                    "javax.persistence.Id"
            ));
            entityClass.getFieldByName(id.get().getName()).get().addAnnotation("Id");
        }
    }
}
