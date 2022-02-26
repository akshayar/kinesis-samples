package com.aksh.kpl.producer;

import com.amazonaws.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class PublisherController {

    @Autowired
    KineisPublisher kineisPublisher;

    @Value("${streamName:aksh-first}")
    String defaultStreamName = "aksh-first";

    @PostMapping("/publish-kpl")
    void newEmployee(@RequestBody String payload, @RequestParam(required = false) String streamName) {
        String stream=Optional.ofNullable(streamName).filter(s->{return !StringUtils.isNullOrEmpty(s); }).orElse(defaultStreamName);
        kineisPublisher.publishKPL(payload, stream);
    }
    @GetMapping("/health-check")
    String ping(){
        return "OK";
    }

}
