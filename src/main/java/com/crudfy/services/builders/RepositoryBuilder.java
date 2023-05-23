package com.crudfy.services.builders;

import com.crudfy.domains.resources.Database;
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

    public void buildRepository(String repositoryPath, String projectName, String entityName, Database database) {

        String interfaceName = nameUtils.getRepositoryClassName(entityName) ;

        CompilationUnit compilationUnit = initialize(nameUtils.getRootImportPath(projectName) + ".repositories", interfaceName,true);
        ClassOrInterfaceDeclaration repositoryInterface = compilationUnit.getInterfaceByName(interfaceName).get();

        addImports(Arrays.asList(
                nameUtils.getEntityImportPath(projectName, entityName),
                "org.springframework.stereotype.Repository"));

        switch (database) {
            case MONGODB:
                addImports(Arrays.asList("org.springframework.data.mongodb.repository.MongoRepository"));
                //TODO Use the id field type
                repositoryInterface.addExtendedType("MongoRepository<" + nameUtils.getBaseClassName(entityName) + ", String>");
                break;
            case ELASTICSEARCH:
                addImports(Arrays.asList("org.springframework.data.elasticsearch.repository.ElasticsearchRepository"));
                repositoryInterface.addExtendedType("ElasticsearchRepository<" + nameUtils.getBaseClassName(entityName) + ", String>");
                break;
            default:
                addImports(Arrays.asList("org.springframework.data.repository.CrudRepository"));
                repositoryInterface.addExtendedType("CrudRepository<" + nameUtils.getBaseClassName(entityName) + ", String>");
                break;
        }
        addAnnotation("Repository");
        write(repositoryPath, "Erro na escrita da interface Repository");
    }
}
