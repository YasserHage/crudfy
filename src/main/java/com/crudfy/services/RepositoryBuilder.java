package com.crudfy.services;

import com.crudfy.services.utils.NameUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;

@Service
public class RepositoryBuilder {

    @Autowired
    private NameUtils nameUtils;

    public void buildRepository(String repositoryPath, String projectName) {

        CompilationUnit compilationUnit = new CompilationUnit();
        String interfaceName = nameUtils.getRepositoryClassName(projectName) ;

        //Interface and Package
        compilationUnit.setPackageDeclaration("com." + projectName + ".repositories");
        ClassOrInterfaceDeclaration controllerClass = compilationUnit
                .addInterface(interfaceName)
                .setPublic(true)
                .addExtendedType("CrudRepository<" + nameUtils.getBaseClassName(projectName) + ", String>");

        addImports(compilationUnit, projectName);
        addAnnotations(controllerClass);

        try {
            //File Writing
            FileWriter myWriter = new FileWriter(String.format("%s/%s.java", repositoryPath, interfaceName));
            myWriter.write(compilationUnit.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException("Erro na escrita da interface Repository", e);
        }
    }

    private void addImports(CompilationUnit compilationUnit, String projectName) {
        compilationUnit.addImport(nameUtils.getEntityImportPath(projectName));
        compilationUnit.addImport("org.springframework.data.repository.CrudRepository");
        compilationUnit.addImport("org.springframework.stereotype.Repository");
    }

    private void addAnnotations(ClassOrInterfaceDeclaration controllerClass) {
        controllerClass.addAnnotation("Repository");
    }
}
