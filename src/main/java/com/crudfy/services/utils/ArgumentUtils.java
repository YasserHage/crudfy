package com.crudfy.services.utils;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ArgumentUtils {

    public NodeList buildNameArgument(String name) {
        return new NodeList(Arrays.asList((new NameExpr(name))));
    }

    public NodeList buildStatusArgument(Expression expression, String status) {
        return new NodeList(Arrays.asList(expression, new FieldAccessExpr(new NameExpr("HttpStatus"), status)));
    }

    public NodeList buildEmptyStatusArgument(String status) {
        return new NodeList(Arrays.asList(new FieldAccessExpr(new NameExpr("HttpStatus"), status)));
    }

    public NodeList buildArguments(Expression... expressions) {
        return new NodeList(expressions);
    }
}
