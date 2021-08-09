package com.crudfy.services.builders;

import com.crudfy.services.utils.NameUtils;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class RepositoryBuilder extends ClassOrInterfaceBuilder{

    @Autowired
    private NameUtils nameUtils;

    public void buildRepository(String repositoryPath, String projectName) {

        String interfaceName = nameUtils.getRepositoryClassName(projectName) ;

        CompilationUnit compilationUnit = initialize("com." + projectName + ".repositories", interfaceName,true);
        ClassOrInterfaceDeclaration repositoryInterface = compilationUnit.getInterfaceByName(interfaceName).get();
        repositoryInterface.addExtendedType("CrudRepository<" + nameUtils.getBaseClassName(projectName) + ", String>");

        addImports(Arrays.asList(
                nameUtils.getEntityImportPath(projectName),
                "org.springframework.data.repository.CrudRepository",
                "org.springframework.stereotype.Repository"));
        addAnnotations(Arrays.asList("Repository"));

        write(repositoryPath, "Erro na escrita da interface Repository");
    }
}
