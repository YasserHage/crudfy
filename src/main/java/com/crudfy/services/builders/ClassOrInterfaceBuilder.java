package com.crudfy.services.builders;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class ClassOrInterfaceBuilder {

    private CompilationUnit compilationUnit;
    private String classOrInterfaceName;
    private boolean isInterface;

    public CompilationUnit initialize(String packagePath, String classOrInterfaceName, boolean isInterface) {
        compilationUnit = new CompilationUnit();
        this.classOrInterfaceName = classOrInterfaceName;
        this.isInterface = isInterface;
        compilationUnit.setPackageDeclaration(packagePath);
        if (isInterface) {
            compilationUnit.addInterface(classOrInterfaceName).setPublic(true);
        } else {
            compilationUnit.addClass(classOrInterfaceName).setPublic(true);
        }
        return compilationUnit;
    }

    public void addImports(List<String> imports) {
        imports.forEach(compilationUnit::addImport);
    }

    public void addAnnotations(List<String> annotations) {
        ClassOrInterfaceDeclaration classOrInterface = isInterface ?
                compilationUnit.getInterfaceByName(classOrInterfaceName).get() :
                compilationUnit.getClassByName(classOrInterfaceName).get();

        annotations.forEach(classOrInterface::addAnnotation);
    }

    public void addAnnotation(String name) {
        ClassOrInterfaceDeclaration classOrInterface = isInterface ?
                compilationUnit.getInterfaceByName(classOrInterfaceName).get() :
                compilationUnit.getClassByName(classOrInterfaceName).get();

        NormalAnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(name);
        classOrInterface.addAnnotation(annotation);
    }

    public void addAnnotation(String name, Map<String, String> params) {
        ClassOrInterfaceDeclaration classOrInterface = isInterface ?
                compilationUnit.getInterfaceByName(classOrInterfaceName).get() :
                compilationUnit.getClassByName(classOrInterfaceName).get();

        NormalAnnotationExpr annotation = new NormalAnnotationExpr();
        if (params != null) {
            params.forEach((param,value) -> annotation.addPair(param, value));
        }
        annotation.setName(name);

        classOrInterface.addAnnotation(annotation);
    }

    public void write(String path, String errorMessage) {
        try {
            //File Writing
            FileWriter myWriter = new FileWriter(String.format("%s/%s.java", path, classOrInterfaceName));
            myWriter.write(compilationUnit.toString());
            myWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }
}
