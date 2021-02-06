package com.cowtowncoder.jackformer.webapp;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController
{
    @RequestMapping("/hello")
    public String hello() {
        return "Hello!";
   }
}
