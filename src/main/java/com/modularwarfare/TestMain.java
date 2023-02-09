package com.modularwarfare;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.modularwarfare.common.vector.Matrix4f;
import com.modularwarfare.common.vector.Vector3f;

public class TestMain {
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
    private static int count=0;
    public static void main(String[] args) {
        for(int i=0;i<1000;i++) {
            executor.execute(()->{
                System.out.println(count++);
            });
        }
    }

}
