package dev.quickanswer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server")
public class ServerController {
    @GetMapping
    public ResponseEntity<String> serverStatus() {
        return ResponseEntity.ok("Server is live ");
    }
}
