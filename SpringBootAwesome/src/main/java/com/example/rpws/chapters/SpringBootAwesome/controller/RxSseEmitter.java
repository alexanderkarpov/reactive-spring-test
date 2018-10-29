package com.example.rpws.chapters.SpringBootAwesome.controller;

import com.example.rpws.chapters.SpringBootAwesome.domain.Temperature;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import rx.Subscriber;

import java.io.IOException;

@Slf4j
@Getter
class RxSseEmitter extends SseEmitter {

    private static final long SSE_SESSION_TIMEOUT = 30 * 60 * 1000L;
    private final Subscriber<Temperature> subscriber;

    RxSseEmitter() {
        super(SSE_SESSION_TIMEOUT);

        this.subscriber = new Subscriber<Temperature>() {
            @Override
            public void onCompleted() {
                log.info("subscription completed");
            }

            @Override
            public void onError(Throwable throwable) {
                log.warn("an error occurred", throwable);
            }

            @Override
            public void onNext(Temperature temperature) {
                try {
                    RxSseEmitter.this.send(temperature);
                } catch (IOException ex) {
                    log.warn("can't send {}", temperature);
                    unsubscribe();
                }
            }
        };
    }
}
