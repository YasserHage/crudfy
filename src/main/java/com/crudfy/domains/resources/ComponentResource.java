package com.crudfy.domains.resources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class ComponentResource {
    private String name;
    private String path;
    private Database database = Database.MYSQL;
    private List<Entity> entities;
}
