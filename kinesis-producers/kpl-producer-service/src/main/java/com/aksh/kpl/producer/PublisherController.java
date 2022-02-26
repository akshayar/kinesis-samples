package com.aksh.kpl.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublisherController {

    @Autowired
    KineisPublisher kineisPublisher;

    @PostMapping("/publish-kpl")
    void newEmployee(@RequestBody String payload) {
        kineisPublisher.publishKPL(payload);
    }
    @GetMapping("/health-check")
    String ping(){
        return "OK";
    }

}
