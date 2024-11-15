package edu.washu.tag.telemetrydemo;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/")
public class Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
    private final AttributeKey<String> ATTR_METHOD = AttributeKey.stringKey("method");

    private final Random random = new Random();
    private final Tracer tracer;
    private final LongCounter pizzaCounter;
    private final LongHistogram pizzaHistogram;

    @Autowired
    Controller(OpenTelemetry openTelemetry) {
        tracer = openTelemetry.getTracer(Application.class.getName());
        Meter meter = openTelemetry.getMeter(Application.class.getName());
        pizzaCounter = meter.counterBuilder("pizza-counter").build();
        pizzaHistogram = meter.histogramBuilder("pizza-duration").ofLongs().build();
    }

    @GetMapping("/")
    public String hello() {
        return "Hello from Spring Boot!";
    }

    @GetMapping("/user")
    public String user() {
        return "Hello User!";
    }

    @GetMapping("/admin")
    public String admin() {
        return "Hello Admin!";
    }

    @GetMapping("/pizza")
    public String pizza() throws InterruptedException, OutOfToppingException, BurntPizzaException {
        long startTime = System.currentTimeMillis();
        Span span = tracer.spanBuilder("pizza").startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.addEvent("Start making pizza");
            makeDough();
            addToppings();
            bakePizza();
            span.addEvent("Pizza is ready");
        } finally {
            span.end();
        }
        long duration = System.currentTimeMillis() - startTime;
        pizzaCounter.add(1);
        pizzaHistogram.record(duration, Attributes.of(ATTR_METHOD, "pizza"));
        return "Your pizza is ready!";
    }

    private void makeDough() throws InterruptedException {
        Span span = tracer.spanBuilder("makeDough").startSpan();
        try (Scope scope = span.makeCurrent()) {
            Thread.sleep(random.nextInt(100));
            LOGGER.info("Dough is ready");
        } finally {
            span.end();
        }
    }

    private void addToppings() throws InterruptedException, OutOfToppingException {
        Span span = tracer.spanBuilder("addToppings").startSpan();
        try (Scope scope = span.makeCurrent()) {
            if (random.nextInt(10) < 1) { // 10% chance to throw OutOfToppingException
                throw new OutOfToppingException("Ran out of toppings!");
            }
            Thread.sleep(random.nextInt(100));
            LOGGER.info("Toppings are added");
        } finally {
            span.end();
        }
    }

    private void bakePizza() throws InterruptedException, BurntPizzaException {
        Span span = tracer.spanBuilder("bakePizza").startSpan();
        try (Scope scope = span.makeCurrent()) {
            if (random.nextInt(10) < 1) { // 10% chance to throw BurntPizzaException
                throw new BurntPizzaException("Pizza is burnt!");
            }
            Thread.sleep(random.nextInt(100));
            LOGGER.info("Pizza is baked");
        } finally {
            span.end();
        }
    }

}

class OutOfToppingException extends Exception {
    public OutOfToppingException(String message) {
        super(message);
    }
}

class BurntPizzaException extends Exception {
    public BurntPizzaException(String message) {
        super(message);
    }
}
