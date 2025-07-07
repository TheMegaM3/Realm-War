package com.realmwar.engine;

import java.util.Timer;
import java.util.TimerTask;

public class TurnTimer {
    private Timer timer;
    private static final int TURN_DURATION = 30_000;
    private final Runnable timeOutCallback;

    public TurnTimer(Runnable timeOutCallback) {
        this.timeOutCallback = timeOutCallback;
    }
    public void start() {
        stop();
        timer = new Timer();
        timer.schedule(new  TimerTask() {
            @Override
            public void run() {
                System.out.println("Turn timer expired. Ending turn automatically.");
                timeOutCallback.run();
            }
        }, TURN_DURATION);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
