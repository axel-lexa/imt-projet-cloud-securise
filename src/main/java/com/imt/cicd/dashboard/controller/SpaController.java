package com.imt.cicd.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    // Modification ici : ajout de "/**/{path:[^\\.]*}" pour gérer les sous-dossiers
    // Cela capture /dashboard, mais aussi /pipelines/1, /history/details/5, etc.
    @RequestMapping(value = {"/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public String redirect() {
        // On transfère la requête à index.html qui chargera React
        return "forward:/index.html";
    }
}