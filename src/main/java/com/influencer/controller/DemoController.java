package com.influencer.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class DemoController {

        @GetMapping("/welcome")
        public String welcomeMessage() {

            return "Welcome to Influencer API!";
        }





        @GetMapping("/authenticated")
        public String checkAuthenticated() {

            return "Yes you are authenticated";

        }




}
