package com.example;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;

public class Main {

    public static void main(String... args) {

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
                        () -> System.out.println("onComplete"));


    }


}
