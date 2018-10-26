package com.example.rpws.chapters.SpringBootAwesome.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.example.rpws.chapters.SpringBootAwesome.domain.Temperature;

import rx.Observable;

@Component
public class TemperatureSensor {

    private final Random rnd = new Random();

    private final Observable<Temperature> dataStream =
            Observable
                    .range(0, Integer.MAX_VALUE)
                    .concatMap(tick -> Observable
                            .just(tick)
                            .delay(rnd.nextInt(5000), TimeUnit.MILLISECONDS)
                            .map(tickValue -> this.probe()))
                    .publish()
                    .refCount();

    private Temperature probe() {
        return new Temperature(16 + rnd.nextGaussian() * 10);
    }

    public Observable<Temperature> temperatureStream() {
        return dataStream;
    }
}
