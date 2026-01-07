package com.imt.cicd.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    // On liste explicitement les routes React pour éviter de capturer "/ws" ou "/api"
    // Si vous ajoutez une page React, ajoutez son chemin ici.
    @RequestMapping(value = {
            "/",
            "/login",
            "/dashboard/**",
            "/pipeline/**",
            "/history/**",
            "/deployments/**",
            "/users/**"
    })
    public String redirect() {
        return "forward:/index.html";
    }
}