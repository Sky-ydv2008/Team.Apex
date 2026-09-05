package com.apexinnovators.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoogleVerificationController {

    @GetMapping(value = "/googlea24d7602356f2812.html", produces = MediaType.TEXT_HTML_VALUE)
    public String googleVerification() {
        return "google-site-verification: googlea24d7602356f2812.html";
    }
}
