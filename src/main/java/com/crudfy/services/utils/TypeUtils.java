package com.crudfy.services.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.springframework.stereotype.Component;

@Component
public class TypeUtils {

    JavaParser parser = new JavaParser();

    public ClassOrInterfaceType getClassOrInterfaceType(String type) {
        return parser.parseClassOrInterfaceType(type).getResult().get();
    }

    public ClassOrInterfaceType getStringType() {
        return parser.parseClassOrInterfaceType("String").getResult().get();
    }

    public ClassOrInterfaceType getResponseEntityType(String type) {
        return parser.parseClassOrInterfaceType(String.format("ResponseEntity<%s>", type)).getResult().get();
    }
}
