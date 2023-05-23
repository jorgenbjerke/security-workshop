package com.github.jorgenbjerke.security.workshop;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "api-v1")
@RestController
@RequestMapping("api/v1")
public class Controller {

    @GetMapping
    public Map<String, String> get() {
        return Map.of("value", "hello world");
    }

}
