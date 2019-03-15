package com.hanshow.support.permission.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

	@GetMapping("/index")
    private String sayHello(){
        System.out.println("Hello World");
        return "Hello World";
    }
}
