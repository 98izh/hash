package com.proxidize_assienment.hash.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@RestController
public class HashController {

    private final Timer requestTimer;
    private final Counter errorCounter;
    private final Counter requestCounter;

    public HashController(MeterRegistry registry) {
        this.requestTimer = Timer.builder("app.requests.seconds")
            .description("Request processing time")
            .tag("service", "hash")
            .register(registry);
        
        this.errorCounter = Counter.builder("app.errors.total")
            .description("Total calculation errors")
            .tag("service", "hash")
            .register(registry);

        this.requestCounter = Counter.builder("app.requests.total")
            .description("Total requests processed")
            .tag("service", "hash")
            .register(registry);
    }

    @PostMapping(value = "/hash", produces = MediaType.TEXT_PLAIN_VALUE)
    public String computeHash(@RequestBody String text) {
        requestCounter.increment();
        long start = System.nanoTime();
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes) + "\n";
        } catch (NoSuchAlgorithmException e) {
            errorCounter.increment();
            throw new RuntimeException("SHA-256 algorithm not found", e);
        } finally {
            long duration = System.nanoTime() - start;
            requestTimer.record(duration, TimeUnit.NANOSECONDS);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}