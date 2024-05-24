package com.crudfy.domains.resources;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Field {
    private String name;
    private String type;
    private boolean isId;
    private boolean isSubEntity;
}
