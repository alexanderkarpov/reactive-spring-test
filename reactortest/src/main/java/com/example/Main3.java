package com.example;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class Main3 {

    public static void main(String ... args) throws InterruptedException {
        Flux.range(0, 10000)

                .parallel()
                .runOn(Schedulers.parallel(), 10)
                .subscribe(new CustomSubscriber<>());


        Thread.sleep(1000);
    }

}
