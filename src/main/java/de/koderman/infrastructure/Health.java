package de.koderman.infrastructure;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
class Health {
    @GetMapping("/healthz")
    public Map<String, String> ok() { 
        return Map.of("status", "ok"); 
    }
}