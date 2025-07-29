package com.restaurantapp.controller;

import com.restaurantapp.dto.Feedback;
import com.restaurantapp.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class FeedbackController {
    public FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/feedbacks")
    public ResponseEntity<String> postFeedbackController(@RequestBody String requestBody) throws Exception {
        Feedback feedback = Feedback.fromJson(requestBody);
        String result = feedbackService.postFeedbackService(feedback);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/feedbacks")
    public ResponseEntity<String> putFeedbackController(@RequestBody String requestBody) throws Exception {
        Feedback feedback = Feedback.fromJson(requestBody);
        String result = feedbackService.putFeedbackService(feedback);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/waiters-info")
    public ResponseEntity<Object> getWaiterDetailsController( @RequestParam(required = false) Map<String, String> allParams) throws Exception {
        return new ResponseEntity<>(feedbackService.getWaiterDetailsService(allParams),HttpStatus.OK);
    }


}
