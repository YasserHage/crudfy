package com.crudfy.services;

import com.crudfy.domains.Field;
import com.crudfy.domains.ImportsMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class DomainBuilder {

    @Autowired
    private ImportsMapper mapper;

    public void buildResponse(String projectName, String domainPath, List<Field> fields) {

        String className = projectName.substring(0, 1).toUpperCase() + projectName.substring(1) + "Response";
        CompilationUnit compilationUnit = addCommons(projectName, fields, className);

        try {
            //File Writing
            FileWriter myWriter = new FileWriter(String.format("%s/%s.java", domainPath, className));
            myWriter.write(compilationUnit.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Erro na escrita da classe Response", e);
        }

    }

    public void buildResource(String projectName, String domainPath, List<Field> fields) {

        String className = projectName.substring(0, 1).toUpperCase() + projectName.substring(1) + "Resource";
        CompilationUnit compilationUnit = addCommons(projectName, fields, className);

        try {
            //File Writing
            FileWriter myWriter = new FileWriter(String.format("%s/%s.java", domainPath, className));
            myWriter.write(compilationUnit.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Erro na escrita da classe Resource", e);
        }
    }

    public void buildEntity(String projectName, String domainPath, List<Field> fields) {

        String className = projectName.substring(0, 1).toUpperCase() + projectName.substring(1);
        CompilationUnit compilationUnit = addCommons(projectName, fields, className);

        try {
            //File Writing
            FileWriter myWriter = new FileWriter(String.format("%s/%s.java", domainPath, className));
            myWriter.write(compilationUnit.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Erro na escrita da classe Entity", e);
        }
    }

    private CompilationUnit addCommons(String projectName, List<Field> fields, String className) {
        CompilationUnit compilationUnit = new CompilationUnit();

        //Class and Package
        compilationUnit.setPackageDeclaration("com." + projectName + ".domains");
        ClassOrInterfaceDeclaration commonClass = compilationUnit.addClass(className).setPublic(true);

        addCommonImports(compilationUnit);
        addCommonAnnotations(commonClass);
        addFields(compilationUnit, commonClass, fields);

        return compilationUnit;
    }

    private void addCommonImports(CompilationUnit compilationUnit) {
        compilationUnit.addImport("lombok.Data");
        compilationUnit.addImport("lombok.AllArgsConstructor");
        compilationUnit.addImport("lombok.NoArgsConstructor");
    }

    private void addCommonAnnotations(ClassOrInterfaceDeclaration commonClass) {
        commonClass.addAnnotation("Data");
        commonClass.addAnnotation("AllArgsConstructor");
        commonClass.addAnnotation("NoArgsConstructor");
    }

    private void addFields(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration commonClass, List<Field> fields) {
        JavaParser parser = new JavaParser();
        fields.forEach(field -> {
            //Types
            Type fieldType = parser.parseType(field.getType()).getResult().get();
            commonClass.addField(fieldType, field.getName(), Modifier.Keyword.PRIVATE);
            mapper.getImport(field.getType()).forEach(fieldImport -> {
                compilationUnit.addImport(fieldImport);
            });
        });
    }
}
