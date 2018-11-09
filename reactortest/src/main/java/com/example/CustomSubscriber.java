package com.example;

import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Subscription;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Value
@AllArgsConstructor
public class CustomSubscriber<T> extends BaseSubscriber<T> {

    private static final AtomicLong id = new AtomicLong();

    String name;
    long request;
    long processingTimeout;

    CustomSubscriber() {
        this(Long.MAX_VALUE, 0);
    }

    CustomSubscriber(long request, long processingTimeout) {
        this("subscriber#" + id.getAndIncrement(), request, processingTimeout);
    }

    CustomSubscriber(String name) {
        this("subscriber#" + name, Long.MAX_VALUE, 0);
    }

    @Override
    protected void hookOnComplete() {
        log.info("{}: OnComplete", name);
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        log.info("{}: onSubscribe: {}", name, subscription);
        request(request);
    }

    @Override
    protected void hookOnNext(T value) {
        log.info("{}: onNext: {}", name, value);
        if (processingTimeout > 0) {
            try {
                Thread.sleep(processingTimeout);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        request(request);
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        log.info("{}: onError", name, throwable);
    }

    @Override
    protected void hookOnCancel() {
        log.info("{}: onCancel");
    }

    @Override
    protected void hookFinally(SignalType type) {
        log.info("{}: finally", name);
    }
}
