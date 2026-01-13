package com.example.metricsdemo.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);

    private final AtomicInteger queueSize = new AtomicInteger(0);
    private final Counter requestCounter;
    private final Timer processTimer;

    public MetricsController(MeterRegistry meterRegistry) {

        // 1. Counter: Counts the number of requests
        this.requestCounter = Counter.builder("custom_request_count")
                .description("Total number of requests to the custom endpoint")
                .tags("region", "us-east")
                .register(meterRegistry);

        // 2. Timer: Measures the time taken to process a request
        this.processTimer = Timer.builder("custom_process_duration")
                .description("Time taken to process the request")
                .register(meterRegistry);

        // 3. Gauge: Measures the current value of queue size
        // Gauges are useful for monitoring state that goes up and down
        Gauge.builder("custom_queue_size", queueSize, AtomicInteger::get)
                .description("Current size of the processing queue")
                .register(meterRegistry);
    }

    @PostMapping("/process")
    public String processRequest(@RequestParam(defaultValue = "100") int durationMs) {
        // Increment counter
        requestCounter.increment();
        logger.info("Received process request with duration: {}ms", durationMs);

        // Increment queue (simulating work arriving)
        int currentQueueSize = queueSize.incrementAndGet();
        logger.info("Added to queue. Current size: {}", currentQueueSize);

        // Record time
        processTimer.record(() -> {
            try {
                // Simulate processing
                TimeUnit.MILLISECONDS.sleep(durationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Processing interrupted", e);
            }
        });

        // Decrement queue (simulating work finished)
        currentQueueSize = queueSize.decrementAndGet();
        logger.info("Request processed. Removed from queue. Current size: {}", currentQueueSize);

        return "Processed in " + durationMs + "ms";
    }

    @PostMapping("/queue/add")
    public String addToQueue() {
        int size = queueSize.incrementAndGet();
        logger.info("Manual queue add. Current size: {}", size);
        return "Added to queue. Current size: " + size;
    }

    @PostMapping("/queue/remove")
    public String removeFromQueue() {
        int size = queueSize.decrementAndGet();
        logger.info("Manual queue remove. Current size: {}", size);
        return "Removed from queue. Current size: " + size;
    }

    @GetMapping("/hello")
    public String hello() {
        requestCounter.increment();
        logger.info("Hello endpoint called");
        return "Hello World!";
    }
}
