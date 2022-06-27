package me.laravieira.willy.utils;

import java.util.Date;

public class PassedInterval {
    private long interval;
    private long begin;

    public PassedInterval(long interval) {
        this.interval = interval;
    }

    public void start() {
        this.begin = new Date().getTime();
    }

    public void reset() {
        this.start();
    }

    public boolean hasPassedInterval() {
        return begin + interval <= new Date().getTime();
    }
}
