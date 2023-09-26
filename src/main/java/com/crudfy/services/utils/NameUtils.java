package com.crudfy.services.utils;

import com.crudfy.domains.resources.Structure;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.stereotype.Component;

import static com.crudfy.domains.resources.Structure.LAYER;

@Component
public class NameUtils {

    public String getBaseClassName(String entityName) {
        return entityName.toLowerCase().substring(0, 1).toUpperCase() + entityName.substring(1);
    }

    public String getMainClassName(String projectName) {
        return getBaseClassName(projectName) + "Application";
    }

    public String getResponseClassName(String entityName) {
        return getBaseClassName(entityName) + "Response";
    }

    public String getResourceClassName(String entityName) {
        return getBaseClassName(entityName) + "Resource";
    }

    public String getControllerClassName(String entityName) {
        return getBaseClassName(entityName) + "Controller";
    }

    public String getRepositoryClassName(String entityName) {
        return getBaseClassName(entityName) + "Repository";
    }

    public String getServiceClassName(String entityName) {
        return getBaseClassName(entityName) + "Service";
    }

    public String getMapperClassName(String entityName) {
        return getBaseClassName(entityName) + "Mapper";
    }

    public String getResourceVariableName(String entityName) {
        return entityName.toLowerCase() + "Resource";
    }

    public String getResponseVariableName(String entityName) {
        return entityName.toLowerCase() + "Response";
    }

    public String getRepositoryVariableName(String entityName) {
        return entityName.toLowerCase() + "Repository";
    }

    public String getServiceVariableName(String entityName) {
        return entityName.toLowerCase() + "Service";
    }

    public String getMapperVariableName(String entityName) {
        return entityName.toLowerCase() + "Mapper";
    }

    public String toEntityMethod(String entityName) {
        return "to" + getBaseClassName(entityName);
    }

    public String toResponseMethod(String entityName) {
        return "to" + getResponseClassName(entityName);
    }

    public String toResponseListMethod(String entityName) {
        return "to" + getResponseClassName(entityName) + "List";
    }


    public String getRootImportPath(String projectName) {
        return String.format("com.%s", projectName.toLowerCase());
    }

    public String getDomainImportPath(String projectName, String entityName, Structure projectStructure) {
        return LAYER.equals(projectStructure) 
                ? getRootImportPath(projectName) + ".domains" 
                : String.format("%s.%s.domains", getRootImportPath(projectName), entityName.toLowerCase());
    }

    public String getResponseImportPath(String projectName, String entityName, Structure projectStructure) {
        return  getDomainImportPath(projectName, entityName, projectStructure) + "." + getResponseClassName(entityName);
    }

    public String getResourceImportPath(String projectName, String entityName, Structure projectStructure) {
        return  getDomainImportPath(projectName, entityName, projectStructure) + "." + getResourceClassName(entityName);
    }

    public String getEntityImportPath(String projectName, String entityName, Structure projectStructure) {
        return  getDomainImportPath(projectName, entityName, projectStructure) + "." + getBaseClassName(entityName);
    }

    public String getRepositoryBaseImportPath(String projectName, String entityName, Structure projectStructure) {
        return LAYER.equals(projectStructure)
                ? getRootImportPath(projectName) + ".repositories"
                : String.format("%s.%s.repositories", getRootImportPath(projectName), entityName.toLowerCase());
    }

    public String getRepositoryImportPath(String projectName, String entityName, Structure projectStructure) {
        return  getRepositoryBaseImportPath(projectName, entityName, projectStructure) + "." + getRepositoryClassName(entityName);
    }

    public String getBaseServiceImportPath(String projectName, String entityName, Structure projectStructure) {
        return LAYER.equals(projectStructure)
                ? getRootImportPath(projectName) + ".services"
                : String.format("%s.%s.services", getRootImportPath(projectName), entityName.toLowerCase());
    }

    public String getServiceImportPath(String projectName, String entityName, Structure projectStructure) {
        return  getBaseServiceImportPath(projectName, entityName, projectStructure) + "." + getServiceClassName(entityName);
    }

    public String getMainRootPath(String basePath, String projectName) {
        return String.format("%s/src/main/java/com/%s", basePath, projectName.toLowerCase());
    }
    
    public String getEntityRootPath(String basePath, String projectName, String entityName) {
        return String.format("%s/%s", getMainRootPath(basePath, projectName), entityName.toLowerCase());
    }

    public String getTestRootPath(String basePath, String projectName) {
        return String.format("%s/src/test/java/com/%s", basePath, projectName.toLowerCase());
    }

    public String getResourcePath(String basePath) {
        return String.format("%s/src/main/resources", basePath);
    }

    public String getControllerPath(String basePath, String projectName, String entityName) {
        return getModulePath(basePath, projectName, entityName, "controllers");
    }

    public String getServicePath(String basePath, String projectName, String entityName) {
        return getModulePath(basePath, projectName, entityName, "services");
    }

    public String getDomainPath(String basePath, String projectName, String entityName) {
        return getModulePath(basePath, projectName, entityName, "domains");
    }

    public String getRepositoryPath(String basePath, String projectName, String entityName) {
        return getModulePath(basePath, projectName, entityName, "repositories");
    }
    
    public String getModulePath(String basePath, String projectName, String entityName, String moduleName) {
        return entityName == null
                ? String.format("%s/%s", getMainRootPath(basePath, projectName), moduleName)
                : String.format("%s/%s", getEntityRootPath(basePath, projectName, entityName), moduleName);
    }

}
