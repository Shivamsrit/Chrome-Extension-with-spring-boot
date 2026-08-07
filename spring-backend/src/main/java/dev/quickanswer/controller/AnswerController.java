package dev.quickanswer.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import dev.quickanswer.dto.AnswerResponse;
import dev.quickanswer.dto.ScreenshotRequest;
import dev.quickanswer.service.AnswerService;

@RestController
@RequestMapping("/api")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping(value = "/answer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public AnswerResponse answer(@RequestBody ScreenshotRequest request) {
        return answerService.answer(request);
    }
}