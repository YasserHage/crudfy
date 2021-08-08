package com.crudfy.services.utils;

import org.springframework.stereotype.Component;

@Component
public class NameUtils {

    public String getBaseClassName(String projectName) {
        return projectName.substring(0, 1).toUpperCase() + projectName.substring(1);
    }

    public String getMainClassName(String projectName) {
        return getBaseClassName(projectName) + "Application";
    }

    public String getResponseClassName(String projectName) {
        return getBaseClassName(projectName) + "Response";
    }

    public String getResourceClassName(String projectName) {
        return getBaseClassName(projectName) + "Resource";
    }

    public String getResourceVariableName(String projectName) {
        return projectName + "Resource";
    }

    public String getRepositoryVariableName(String projectName) {
        return projectName + "Repository";
    }

    public String getMapperVariableName(String projectName) {
        return projectName + "Mapper";
    }

    public String getControllerClassName(String projectName) {
        return getBaseClassName(projectName) + "Controller";
    }

    public String getRepositoryClassName(String projectName) {
        return getBaseClassName(projectName) + "Repository";
    }

    public String getServiceClassName(String projectName) {
        return getBaseClassName(projectName) + "Service";
    }

    public String getMapperClassName(String projectName) {
        return getBaseClassName(projectName) + "Mapper";
    }

    public String toEntityMethod(String projectName) {
        return "to" + getBaseClassName(projectName);
    }

    public String toResponseMethod(String projectName) {
        return "to" + getResponseClassName(projectName);
    }

    public String toResponseListMethod(String projectName) {
        return "to" + getResponseClassName(projectName) + "List";
    }


    public String getRootImportPath(String projectName) {
        return String.format("com.%s", projectName);
    }

    public String getResponseImportPath(String projectName) {
        return  getRootImportPath(projectName) + ".domains." + getResponseClassName(projectName);
    }

    public String getResourceImportPath(String projectName) {
        return  getRootImportPath(projectName) + ".domains." + getResourceClassName(projectName);
    }

    public String getEntityImportPath(String projectName) {
        return  getRootImportPath(projectName) + ".domains." + getBaseClassName(projectName);
    }

    public String getRepositoryImportPath(String projectName) {
        return  getRootImportPath(projectName) + ".repositories." + getRepositoryClassName(projectName);
    }

    public String getMainRootPath(String basePath, String projectName) {
        return String.format("%s/src/main/java/com/%s", basePath, projectName);
    }

    public String getTestRootPath(String basePath, String projectName) {
        return String.format("%s/src/test/java/com/%s", basePath, projectName);
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
