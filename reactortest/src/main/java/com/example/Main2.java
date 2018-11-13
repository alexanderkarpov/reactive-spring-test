package com.example;

import java.time.Duration;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

@Slf4j
public class Main2 {

    public static void main(String... args) throws InterruptedException {


        log.info("--- Hot and cold streams ---");
        Flux<String> coldPublisher = Flux.defer(() -> {
            log.info("Generating new items");
            return Flux.just(UUID.randomUUID().toString());
        });
        log.info("No data was generated so far");
        coldPublisher.subscribe(new CustomSubscriber<>());
        coldPublisher.subscribe(new CustomSubscriber<>());
        log.info("Data was generated twice for two subscribers");

        Flux<Integer> source = Flux.range(0, 3)

                .doOnSubscribe(s -> log.info("new subscription for the cold publisher: {}", s));
        log.info("source flux created");
        ConnectableFlux<Integer> conn = source.publish();
        log.info("connectable flux created");

        conn.subscribe(new CustomSubscriber<>());
        conn.subscribe(new CustomSubscriber<>());

        log.info("all subscribers are ready, connecting");
        conn.connect();


        log.info("--- Caching elements of a stream ---");
        Flux<Integer> source1 = Flux.range(0, 2).doOnSubscribe(s -> log.info("new subscription for the cold publisher"));
        Flux<Integer> cachedSource = source1.cache(Duration.ofSeconds(1));

        cachedSource.subscribe(new CustomSubscriber<>("caching-1"));
        cachedSource.subscribe(new CustomSubscriber<>("caching-2"));

        Thread.sleep(1200);

        cachedSource.subscribe(new CustomSubscriber<>("caching-3"));
        Thread.sleep(1200);



    }

}
