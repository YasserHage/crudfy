package com.crudfy.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ImportsMapper {

    @Autowired
    private ResourceLoader loader;

    private Map<String, List<String>> imports = new HashMap<>();

    @PostConstruct
    private void populate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream importResource = loader.getResource("classpath:imports-mapping.json").getInputStream();
        imports.putAll(mapper.readValue(importResource, Map.class));
    }

    public List<String> getImport(String type) {
        return imports.getOrDefault(type, Arrays.asList(type));
    }
}
