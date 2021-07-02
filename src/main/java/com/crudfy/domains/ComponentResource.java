package com.crudfy.domains;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ComponentResource {
    private String name;
    private String path;
    private List<Field> fields;
}
