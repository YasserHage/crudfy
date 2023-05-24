package com.crudfy.services.utils;

import org.springframework.stereotype.Component;

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

    public String getResponseImportPath(String projectName, String entityName) {
        return  getRootImportPath(projectName) + ".domains." + getResponseClassName(entityName);
    }

    public String getResourceImportPath(String projectName, String entityName) {
        return  getRootImportPath(projectName) + ".domains." + getResourceClassName(entityName);
    }

    public String getEntityImportPath(String projectName, String entityName) {
        return  getRootImportPath(projectName) + ".domains." + getBaseClassName(entityName);
    }

    public String getRepositoryImportPath(String projectName, String entityName) {
        return  getRootImportPath(projectName) + ".repositories." + getRepositoryClassName(entityName);
    }

    public String getServiceImportPath(String projectName, String entityName) {
        return  getRootImportPath(projectName) + ".services." + getServiceClassName(entityName);
    }

    public String getMainRootPath(String basePath, String projectName) {
        return String.format("%s/src/main/java/com/%s", basePath, projectName.toLowerCase());
    }

    public String getTestRootPath(String basePath, String projectName) {
        return String.format("%s/src/test/java/com/%s", basePath, projectName.toLowerCase());
    }

    public String getResourcePath(String basePath) {
        return String.format("%s/src/main/resources", basePath);
    }

    public String getControllerPath(String basePath, String projectName) {
        return getMainRootPath(basePath, projectName) + "/controllers";
    }

    public String getServicePath(String basePath, String projectName) {
        return getMainRootPath(basePath, projectName) + "/services";
    }

    public String getDomainPath(String basePath, String projectName) {
        return getMainRootPath(basePath, projectName) + "/domains";
    }

    public String getRepositoryPath(String basePath, String projectName) {
        return getMainRootPath(basePath, projectName) + "/repositories";
    }

}
