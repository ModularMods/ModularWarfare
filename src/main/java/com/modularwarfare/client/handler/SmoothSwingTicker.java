package com.modularwarfare.client.handler;

import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;

public class SmoothSwingTicker implements Runnable {

    private long lastTime = System.nanoTime();
    private double delta = 0;

    @Override
    public void run() {

        double amountOfTicks = 60.0;

        while (true) {
            long now = System.nanoTime();
            double ns = 1000000000 / amountOfTicks;
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                RenderParameters.SMOOTH_SWING++;
                delta--;
            }
        }
    }
}
