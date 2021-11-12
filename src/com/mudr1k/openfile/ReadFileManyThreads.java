package com.mudr1k.openfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReadFileManyThreads {

    public static final String MARKER_TEXT = "_!_ResT_!_";
    public static final int THREADS_COUNTER = 3;
    private static volatile boolean end = false;

    public static void main(String[] args) throws FileNotFoundException {
        ReadFileManyThreads my = new ReadFileManyThreads();

        String fileName = "test.txt";
        Scanner scanner = new Scanner(new File(fileName));
        scanner.useDelimiter(MARKER_TEXT);

        Runnable run = () -> {
            try {
                my.register(scanner);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        ExecutorService pool = Executors.newFixedThreadPool(THREADS_COUNTER);
        while (!end) {
            pool.execute(run);
        }
        pool.shutdown();
    }

    private synchronized void register(Scanner scanner) throws InterruptedException {

        if (scanner.hasNext()) {
            readLine(scanner);
        } else {
            end = true;
        }
    }

    private void readLine(Scanner scanner) throws InterruptedException {
            System.out.println("I am thread " + Thread.currentThread().getName() + " and I’ve read "
                    + scanner.next().length() + " chars from the file. Time for some rest!");
            Thread.sleep(10);
    }

}
