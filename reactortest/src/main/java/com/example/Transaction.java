package com.example;

import java.time.Duration;
import java.util.Random;

import org.reactivestreams.Publisher;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class Transaction {

    private static final Random random = new Random();
    private final int id;

    private Transaction(int id) {
        this.id = id;
        log.info("[T: {}] created", id);
    }

    public static Mono<Transaction> beginTransaction() {
        return  Mono.defer(() -> Mono.just(new Transaction(random.nextInt(1000))));
    }

    public Flux<String> insertRows(Publisher<String> rows) {
        return Flux.from(rows)
                .delayElements(Duration.ofMillis(100))
                .flatMap(r -> random.nextInt(10) < 2 ? Mono.error(new RuntimeException("Error: " + r)) : Mono.just(r));
    }

    public Mono<Void> commit() {
        return Mono.defer(() -> {
            log.info("[T: {}] commit", id);
            return random.nextBoolean() ? Mono.empty() : Mono.error(new RuntimeException("Conflict"));
        } );
    }

    public Mono<Void> rollback() {
        return Mono.defer(() -> {
           log.info("[T: {}] rollback", id);
            return random.nextBoolean() ? Mono.empty() : Mono.error(new RuntimeException("Conn error"));
        });
    }
}
