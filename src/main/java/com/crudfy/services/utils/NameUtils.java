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
