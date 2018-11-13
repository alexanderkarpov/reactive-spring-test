package com.example;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

/**
 * https://projectreactor.io/docs/core/release/reference/#which-operator
 */
@Slf4j
public class Main1 {

    public static void main(String... args) throws Exception {

        Flux<String> stream1 = Flux.just("Hello", "world");
        Flux<Integer> stream2 = Flux.fromArray(new Integer[]{1, 2, 3});
        Flux<Integer> stream3 = Flux.fromIterable(Arrays.asList(9, 8, 7));
        Flux<Integer> stream4 = Flux.range(2018, 10);

        Mono<String> stream5 = Mono.just("One");
//        Mono<String> stream6 = Mono.justOrEmpty(null);
        Mono<String> stream7 = Mono.justOrEmpty(Optional.empty());

        stream5.subscribe(log::info);

        Flux<String> empty = Flux.empty();
        Flux<String> never = Flux.never();
        Mono<String> error = Mono.error(new RuntimeException("AAA!!!"));

        Mono<String> defer = Mono.defer(() -> Mono.fromCallable(() -> "PREVED"));
        defer.subscribe(log::info);

        //----------------

        Flux.just("A", "B", "C")
                .subscribe(
                        data -> log.info("onNext: " + data),
                        t -> log.info("error", t),
                        () -> log.info("onComplete 1"));


        //----------------

        Flux.range(1, 100)
                .subscribe(
                        data -> log.info("onNext: " + data),
                        t -> log.info("error", t),
                        () -> log.info("onCompete 2"),
                        subscription -> {
                            subscription.request(4);
                            subscription.cancel();
                        }
                );
        //----------------

        CustomSubscriber<Long> customSubscriber = new CustomSubscriber<>(1, 0);

        AtomicLong atomicLong = new AtomicLong();

        Flux<Long> longFlux = Flux.interval(Duration.ofMillis(50)).map(i -> atomicLong.incrementAndGet());
        longFlux.subscribe(customSubscriber);


        Thread.sleep(300);
        Disposable disposable = longFlux.map(Object::toString).subscribe(log::info);
        Thread.sleep(300);
        customSubscriber.dispose();
        Thread.sleep(200);
        disposable.dispose();

        log.info("----------------");
        //  https://projectreactor.io/docs/core/release/reference/#which-operator
        //----------------
        Flux.range(2018, 5)
                .timestamp()
                .index()
                .subscribe(new CustomSubscriber<>(1, 33));


        //Filtering reactive sequences
        log.info("Filtering reactive sequences");
        Flux.just(1, 2, 3, 4, 5, 6).ignoreElements().subscribe(new CustomSubscriber<>());

        Flux.just(1, 2, 3, 4, 5, 6)
                .takeUntil(n -> n > 3)
                .subscribe(new CustomSubscriber<>());

        log.info("--- takeUntilOther ---");
        Flux<Long> intervalFlux1 = Flux.interval(Duration.ofMillis(100)).skip(Duration.ofMillis(200L)).take(8);
        intervalFlux1.subscribe(new CustomSubscriber<>());
        Flux<Long> intervalFlux2 = Flux.interval(Duration.ofMillis(10)).map(i -> System.currentTimeMillis());
        intervalFlux2.takeUntilOther(intervalFlux1)
                .subscribe(new CustomSubscriber<>());

        Thread.sleep(2000);

        log.info("--- Reducing stream elements ---");
        Flux.just(3, 5, 7, 9, 11, 15, 16, 17).repeat()
                .any(e -> e % 2 == 0)
                .subscribe(new CustomSubscriber<>());

        Flux.range(1, 5)
                .reduce(0, (acc, elem) -> acc + elem)
                .subscribe(new CustomSubscriber<>());

        Flux.range(1, 5)
                .scan(0, (acc, elem) -> acc + elem)
                .subscribe(new CustomSubscriber<>());

        log.info("--- Running average ---");
        int bucketSize = 5;
        Flux.range(1, 500)
                .index()
                .scan(
                        new Integer[bucketSize],
                        (acc, elem) -> {
                            acc[(int) (elem.getT1() % bucketSize)] = elem.getT2();
                            return acc;
                        })
                .skip(bucketSize)
                .map(Arrays::asList)
                .map(list -> list.stream().map(e -> Optional.ofNullable(e).orElse(0)).sorted().collect(Collectors.toList()))
                .map(list -> list.stream().mapToInt(i -> i).sum() * 1.0 / bucketSize)
                .index()
                .subscribe(new CustomSubscriber<>());

        log.info("--- then , thenMany , and thenEmpty operators ---");
        Flux.just(1, 2, 3)
                .thenMany(Flux.just("A", "B", "C"))
//                .then()
//                .thenEmpty(Flux.just(22).then())
                .subscribe(new CustomSubscriber<>());

        log.info("--- Sampling elements ---");
        Flux.range(1, 100).map(i -> "!" + i)
                .delayElements(Duration.ofMillis(1))
                .sample(Duration.ofMillis(20))
                .subscribe(new CustomSubscriber<>());


        Thread.sleep(1000);

        log.info("--- Batching stream elements ---");
        Flux.range(1, 30).buffer(8).subscribe(new CustomSubscriber<>());

        log.info("--- Grouping elements elements ---");
        Flux.range(1, 7)
                .groupBy(e -> e % 2 == 0 ? "Even" : "Odd ")
                .subscribe(groupFlux -> groupFlux.subscribe(new CustomSubscriber<>(groupFlux.key())));


        log.info("--- Peeking elements while sequence processing ---");
//        Flux.just(1, 2, 3).concatWith(Flux.error(new RuntimeException("Conn error")))
//                .doOnEach(s -> log.info("#signal: " + s))
//                .doOnEach(s -> log.info("$signal: " + s))
//                .subscribe(new CustomSubscriber<>());

        Flux.just(1, 2, 3)
                .materialize()
//                .dematerialize()
                .collectList()
                .subscribe(new CustomSubscriber<>());

        Flux.just(1, 2, 3).log()
                .subscribe(new CustomSubscriber<>());


        log.info("--- Creating streams programmatically ---");
        Consumer<FluxSink<Integer>> fluxSinkConsumer = emitter -> IntStream.range(10, 15).forEach(emitter::next);
        Flux<Integer> myFlux = Flux.push(fluxSinkConsumer).delayElements(Duration.ofMillis(50));
        myFlux.subscribe(new CustomSubscriber<>(100, 25));
        myFlux.subscribe(new CustomSubscriber<>(100, 200));
        myFlux.subscribe(new CustomSubscriber<>());
        Thread.sleep(1500);
        myFlux.subscribe(new CustomSubscriber<>());
        Thread.sleep(1500);


        log.info("--- Factory method – generate ---");
        Flux.generate(() -> 0,
                (state, sink) -> {
                    if (state > 10) {
                        sink.complete();
                    } else {
                        sink.next(state);
                    }
                    return state + 2;
                })
                .subscribe(new CustomSubscriber<>());


        log.info("--- Wrapping disposable resources into Reactive Streams ---");

        Flux<String> fluxUsing = Flux.using(
                Connection::newConnection,
                connection -> Flux.fromIterable(connection.getData()),
                Connection::close
        );
        fluxUsing.subscribe(new CustomSubscriber<>());

        Flux.usingWhen(
                Transaction.beginTransaction(),
                transaction -> transaction.insertRows(Flux.just("A", "B", "C")),
                Transaction::commit,
                Transaction::rollback
        ).subscribe(new CustomSubscriber<>());

        Thread.sleep(1500);

        log.info("--- Handling errors ---");
        Flux.just("user-1")
                .flatMap(user ->
                        recommendedBooks(user)
                                .retryBackoff(5, Duration.ofMillis(100))
                                .timeout(Duration.ofSeconds(3))
                                .onErrorResume(e -> Flux.just("The Martian")))
                .subscribe(new CustomSubscriber<>());

        Thread.sleep(15000);
    }

    private static Flux<String> recommendedBooks(String userId) {

        return Flux.defer(() ->
                (new Random()).nextInt(10) < 7 ?
                        Flux.<String>error(new RuntimeException("Err")).delaySequence(Duration.ofMillis(100)) :
                        Flux.just("Blue Mars", "The Expanse").delayElements(Duration.ofMillis(50))
        ).doOnSubscribe(s -> log.info("Request for {}", userId)).doOnError(t -> log.warn("Error: {} {}", t.getMessage(), new Date()));
    }

}
