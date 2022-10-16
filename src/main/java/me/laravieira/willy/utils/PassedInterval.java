package me.laravieira.willy.utils;

import java.util.Date;

public class PassedInterval {
    public static final long DISABLE = -1;

    private final long interval;
    private long begin = 0;
    private boolean enable = false;

    public PassedInterval(long interval) {
        this.interval = interval;
    }

    public void start() {
        if(interval < 0)
            return;
        this.begin = new Date().getTime();
        this.enable = true;
    }

    @SuppressWarnings("unused")
    public void disable() {
        enable = false;
    }

    public boolean isEnable() {
        return enable;
    }

    public void reset() {
        this.begin = new Date().getTime();
    }

    public boolean hasPassedInterval() {
        return enable && begin + interval <= new Date().getTime();
    }

    @SuppressWarnings("unused")
    public long passed() {
        return new Date().getTime() - begin;
    }

    public long remaining() {
        if(hasPassedInterval())
            return 0;
        return begin + interval - new Date().getTime();
    }
}
