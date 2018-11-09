package com.example;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.reactivestreams.Subscription;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

/**
 * https://projectreactor.io/docs/core/release/reference/#which-operator
 */
public class Main {

    public static void main(String... args) throws Exception {

        Flux<String> stream1 = Flux.just("Hello", "world");
        Flux<Integer> stream2 = Flux.fromArray(new Integer[]{1, 2, 3});
        Flux<Integer> stream3 = Flux.fromIterable(Arrays.asList(9, 8, 7));
        Flux<Integer> stream4 = Flux.range(2018, 10);

        Mono<String> stream5 = Mono.just("One");
//        Mono<String> stream6 = Mono.justOrEmpty(null);
        Mono<String> stream7 = Mono.justOrEmpty(Optional.empty());

        stream5.subscribe(System.out::println);

        Flux<String> empty = Flux.empty();
        Flux<String> never = Flux.never();
        Mono<String> error = Mono.error(new RuntimeException("AAA!!!"));

        Mono<String> defer = Mono.defer(() -> Mono.fromCallable(() -> "PREVED"));
        defer.subscribe(System.out::println);

        //----------------

        Flux.just("A", "B", "C")
                .subscribe(
                        data -> System.out.println("onNext: " + data),
                        Throwable::printStackTrace,
                        () -> System.out.println("onComplete 1"));


        //----------------

        Flux.range(1, 100)
                .subscribe(
                        data -> System.out.println("onNext: " + data),
                        Throwable::printStackTrace,
                        () -> System.out.println("onCompete 2"),
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
        Disposable disposable = longFlux.subscribe(System.out::println);
        Thread.sleep(300);
        customSubscriber.dispose();
        Thread.sleep(200);
        disposable.dispose();

        System.out.println("----------------");
        //  https://projectreactor.io/docs/core/release/reference/#which-operator
        //----------------
        Flux.range(2018, 5)
                .timestamp()
                .index()
                .subscribe(new CustomSubscriber<>(1, 33));


        //Filtering reactive sequences
        System.out.println("Filtering reactive sequences");
        Flux.just(1, 2, 3, 4, 5, 6).ignoreElements().subscribe(new CustomSubscriber<>());

        Flux.just(1, 2, 3, 4, 5, 6)
                .takeUntil(n -> n > 3)
                .subscribe(new CustomSubscriber<>());

        System.out.println("--- takeUntilOther ---");
        Flux<Long> intervalFlux1 = Flux.interval(Duration.ofMillis(100)).skip(Duration.ofMillis(200L)).take(8);
        intervalFlux1.subscribe(new CustomSubscriber<>());
        Flux<Long> intervalFlux2 = Flux.interval(Duration.ofMillis(10)).map(i -> System.currentTimeMillis());
        intervalFlux2.takeUntilOther(intervalFlux1)
                .subscribe(new CustomSubscriber<>());

        Thread.sleep(2000);

        System.out.println("--- Reducing stream elements ---");
        Flux.just(3, 5, 7, 9, 11, 15, 16, 17).repeat()
                .any(e -> e % 2 == 0)
                .subscribe(new CustomSubscriber<>());

        Flux.range(1, 5)
                .reduce(0, (acc, elem) -> acc + elem)
                .subscribe(new CustomSubscriber<>());

        Flux.range(1, 5)
                .scan(0, (acc, elem) -> acc + elem)
                .subscribe(new CustomSubscriber<>());

        System.out.println("--- Running average ---");
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

        System.out.println("--- then , thenMany , and thenEmpty operators ---");
        Flux.just(1, 2, 3)
                .thenMany(Flux.just("A", "B", "C"))
//                .then()
//                .thenEmpty(Flux.just(22).then())
                .subscribe(new CustomSubscriber<>());

        System.out.println("--- Sampling elements ---");
        Flux.range(1, 100).map(i -> "!" + i)
                .delayElements(Duration.ofMillis(1))
                .sample(Duration.ofMillis(20))
                .subscribe(new CustomSubscriber<>());


        Thread.sleep(1000);

        System.out.println("--- Batching stream elements ---");
        Flux.range(1, 30).buffer(8).subscribe(new CustomSubscriber<>());

        System.out.println("--- Grouping elements elements ---");
        Flux.range(1, 7)
                .groupBy(e -> e % 2 == 0 ? "Even" : "Odd ")
                .subscribe(groupFlux -> groupFlux.subscribe(new CustomSubscriber<>(groupFlux.key())));


        System.out.println("--- Peeking elements while sequence processing ---");
//        Flux.just(1, 2, 3).concatWith(Flux.error(new RuntimeException("Conn error")))
//                .doOnEach(s -> System.out.println("#signal: " + s))
//                .doOnEach(s -> System.out.println("$signal: " + s))
//                .subscribe(new CustomSubscriber<>());

        Flux.just(1, 2, 3)
                .materialize()
//                .dematerialize()
                .collectList()
                .subscribe(new CustomSubscriber<>());

        Flux.just(1, 2, 3).log()
                .subscribe(new CustomSubscriber<>());
    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    @AllArgsConstructor
    private static class CustomSubscriber<T> extends BaseSubscriber<T> {

        static final AtomicLong id = new AtomicLong();

        String name;
        long request;
        long processingTimeout;

        CustomSubscriber() {
            this(Long.MAX_VALUE, 0);
        }

        CustomSubscriber(long request, long processingTimeout) {
            this("subscriber#" + id.getAndIncrement(), request, processingTimeout);
        }

        public CustomSubscriber(String name) {
            this("subscriber#" + name, Long.MAX_VALUE, 0);
        }

        @Override
        protected void hookOnComplete() {
            System.out.println(name + ": OnComplete" + " [" + Thread.currentThread().getId() + "]");
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            System.out.println(name + ": onSubscribe: " + subscription + " [" + Thread.currentThread().getId() + "]");
            request(request);
        }

        @Override
        protected void hookOnNext(T value) {
            System.out.println(name + ": onNext: " + value + " [" + Thread.currentThread().getId() + "]");
            try {
                Thread.sleep(processingTimeout);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            request(request);
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        protected void hookOnCancel() {
            System.out.println(name + ": onCancel" + " [" + Thread.currentThread().getId() + "]");
        }

        @Override
        protected void hookFinally(SignalType type) {
            System.out.println(name + ": finally" + " [" + Thread.currentThread().getId() + "]");
        }
    }
}
