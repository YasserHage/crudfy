package com.crudfy.controllers;

import com.crudfy.domains.ComponentResource;
import com.crudfy.services.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/crud")
public class CrudController {

    @Autowired
    private CrudService service;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody ComponentResource resource) {
        try {
            service.createProject(resource);
            return new ResponseEntity<>(String.format("Projeto criado com sucesso em %s", resource.getPath()), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Ocorreu um erro ao criar o projeto, favor validar o formato da entrada", e);
        }
    }
}
