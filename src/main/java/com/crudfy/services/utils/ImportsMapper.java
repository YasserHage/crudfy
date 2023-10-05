package com.crudfy.services.utils;

import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class ImportsMapper {

    @Autowired
    private ResourceLoader loader;

    private Map<String, String> imports = new HashMap<>();

    @PostConstruct
    private void populate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream importResource = loader.getResource("classpath:imports-mapping.json").getInputStream();
        imports.putAll(mapper.readValue(importResource, Map.class));
    }

    public List<String> getImports(String fullType) {
        List<String> types = new ArrayList<>();
        if (fullType.contains("<")) {
            String mainType = fullType.substring(0, fullType.indexOf('<'));
            String[] subTypes = fullType.substring(fullType.indexOf('<') + 1, fullType.indexOf('>')).split(",");

            addImport(mainType, types);
            for (String type : subTypes) {
                addImport(fullType, types);
            }
        } else {
            addImport(fullType, types);
        }
        return types;
    }

    private void addImport(String type, List<String> list) {
        String typeImport = imports.get(type);
        if (StringUtils.isNotBlank(typeImport)) {
            list.add(typeImport);
        }
    }
}
