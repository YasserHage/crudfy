package com.crudfy.domains.resources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class ComponentResource {
    private String path;
    private String projectName;
    private Structure projectStructure = Structure.LAYER;
    private Database database = Database.MYSQL;
    private List<Entity> entities;
}
