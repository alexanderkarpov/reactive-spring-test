package com.example;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

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

    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    @AllArgsConstructor
    private static class CustomSubscriber<T> extends BaseSubscriber<T> {

        long request;
        long processingTimeout;

        CustomSubscriber() {
            this(Long.MAX_VALUE, 0);
        }

        @Override
        protected void hookOnComplete() {
            System.out.println("*** OnComplete");
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            System.out.println("*** onSubscribe: " + subscription);
            request(request);
        }

        @Override
        protected void hookOnNext(T value) {
            System.out.println("*** onNext: " + value);
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
            System.out.println("*** onCancel");
        }

        @Override
        protected void hookFinally(SignalType type) {
            System.out.println("*** finally");
        }
    }


}
