package com.mudr1k.openfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ReadFileManyThreads {

    public static final String MARKER_TEXT = "_!_ResT_!_";
    public static final int THREADS_COUNTER = 3;
    private static ExecutorService pool;
    private static Runnable run;
    private static HashMap<String, Long> map = new HashMap<>();
    private ReentrantLock lock = new ReentrantLock();
    private Condition oneThreadSleep = lock.newCondition();

    public static void main(String[] args) throws FileNotFoundException {
        ReadFileManyThreads my = new ReadFileManyThreads();

        String fileName = "test.txt";
        Scanner scanner = new Scanner(new File(fileName));
        scanner.useDelimiter(MARKER_TEXT);

        run = () -> {
            try {
                if (map.get(Thread.currentThread().getName()) == null) {
                    setStartTime();
                }
                my.register(scanner);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (scanner.hasNext()) {
                    pool.execute(run);
                } else {
                    pool.shutdown();
                }
            }
        };

        pool = Executors.newFixedThreadPool(THREADS_COUNTER);

        for (int i = 0; i < THREADS_COUNTER; i++) {
            pool.execute(run);
        }
    }

    private void register(Scanner scanner) throws InterruptedException {
        try {
            lock.lock();
            System.out.print("I am thread " + Thread.currentThread().getName() + " and I start reading after "
                    + getRestTime() + " ms of rest");
            if (scanner.hasNext()) {
                readLine(scanner);
            } else {
                System.out.println(", but nothing");
                oneThreadSleep.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    private void readLine(Scanner scanner) throws InterruptedException {
        System.out.println("\nI am thread " + Thread.currentThread().getName() + " and I’ve read "
                + scanner.next().length() + " chars from the file. Time for some rest!");
        gotoSleep();
        Thread.sleep(10);
    }

    private void gotoSleep() throws InterruptedException {
        setStartTime();
        oneThreadSleep.signalAll();
        oneThreadSleep.await();
    }

    private static void setStartTime() {
        map.put(Thread.currentThread().getName(), System.nanoTime());
    }

    private Long getRestTime() {
        Long time = map.get(Thread.currentThread().getName());
        return time != null ? (System.nanoTime() - time) / 1000000 : 0L;
    }
}

