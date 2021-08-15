package com.crudfy.controllers;

import com.crudfy.domains.exceptions.ResourceValidationException;
import com.crudfy.domains.resources.ComponentResource;
import com.crudfy.domains.responses.ApiError;
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
    public ResponseEntity create(@RequestBody ComponentResource resource) {
        try {
            service.createProject(resource);
            return new ResponseEntity<>(String.format("Projeto criado com sucesso em %s", resource.getPath()),
                    HttpStatus.OK);
        } catch (ResourceValidationException e) {
            return new ResponseEntity<>(new ApiError(e.getMessage(), "Entrada invalida"),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ApiError(String.format("Ocorreu um erro inesperado ao criar o projeto: %s", e.getMessage()), e.getStackTrace().toString()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
