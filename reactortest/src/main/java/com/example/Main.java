package com.example;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Subscription;

import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

public class Main {

    public static void main(String... args) throws Exception {

        Flux<String> stream1 = Flux.just("Hello", "world");
        Flux<Integer> stream2 = Flux.fromArray(new Integer[]{1, 2, 3});
        Flux<Integer> stream3 = Flux.fromIterable(Arrays.asList(9, 8, 7));
        Flux<Integer> stream4 = Flux.range(2018, 10);

        Mono<String> stream5 = Mono.just("One");
        Mono<String> stream6 = Mono.justOrEmpty(null);
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


        BaseSubscriber<Long> customSubscriber = new BaseSubscriber<Long>() {
            @Override
            protected void hookOnComplete() {
                System.out.println("OnComplete");
            }

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                super.hookOnSubscribe(subscription);
                System.out.println("onSubscribe: " + subscription);
            }

            @Override
            protected void hookOnNext(Long value) {
                System.out.println("onNext: " + value);
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            protected void hookOnCancel() {
                System.out.println("onCancel");
            }

            @Override
            protected void hookFinally(SignalType type) {
                System.out.println("finally");
            }
        };

        AtomicLong atomicLong = new AtomicLong();

        Flux<Long> longFlux = Flux.interval(Duration.ofMillis(500)).map(i -> atomicLong.incrementAndGet());
        longFlux.subscribe(customSubscriber);



        Thread.sleep(3000);
        Disposable disposable = longFlux.subscribe(System.out::println);
        Thread.sleep(3000);
        customSubscriber.dispose();
        Thread.sleep(2000);
        disposable.dispose();


    }


}
