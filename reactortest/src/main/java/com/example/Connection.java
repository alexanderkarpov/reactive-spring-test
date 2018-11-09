package com.example;

import java.util.Arrays;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Connection implements AutoCloseable {

    private final Random rnd = new Random();

    private Connection() {
        log.info("IO new Connection created");
    }

    Iterable<String> getData() {
        if (rnd.nextInt(10) < 5) {
            throw new RuntimeException("comm error");
        }
        return Arrays.asList("Some", "data");
    }

    @Override
    public void close() {
        log.info("IO Connection closed");
    }

    static Connection newConnection() {
        return new Connection();
    }

}
