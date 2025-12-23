package com.example.FYP.Api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final int MAX_THREADS = 4;
    private static final long NUMBER_OF_OBJECTS = (long) Math.pow(10, 7);
    private static final AtomicInteger testCase = new AtomicInteger(0);

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException, ExecutionException {
        /*Set<String> officialCountries = new HashSet<>(Arrays.asList(Locale.getISOCountries()));

        BigDecimal bigDecimal = new BigDecimal("23.23");
        System.out.println(bigDecimal);

        // Example: Print the number of official countries
        System.out.println("Number of Officially Recognized Countries: " + officialCountries.size());

        // Print the official country codes
        System.out.println(officialCountries);
        *//*  Method method = Main.class.getMethod("singleThreadTest",long.class);
        Method method2 = Main.class.getMethod("multiThreadTest",long.class);

        long elapsedTime = test(method);
        long elapsedTime2 = test(method2);

        System.out.println("4 Threads is faster than a single thread : " + elapsedTime / elapsedTime2);*/

        long n = 100_000_000_000L;

        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<Future<Long>> futures = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Future<Long> future = executor.submit(() -> test1N(n));
            futures.add(future);
        }

        // Shutdown the executor and wait for all tasks to finish
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        // Retrieve and print results
        for (int i = 0; i < futures.size(); i++) {
            long time = futures.get(i).get();
            System.out.println("Task " + i + " took: " + time + " ms");
        }
    }

    public static long test2N(long n) {
        long start = System.currentTimeMillis();

        int index = 0;
        for (int i = 0; i < n * 2; i++) {
            index++;
        }

        long end = System.currentTimeMillis();
        return end - start;
    }

    public static long test1N(long n) {
        long start = System.currentTimeMillis();

        long index = 0;
        for (long i = 0; i < n; i++) {
            index++;
        }

        long end = System.currentTimeMillis();
        return end - start;
    }


    public static long test(Method method) throws InvocationTargetException, IllegalAccessException {
        System.out.println("TEST " + testCase.getAndIncrement());
        long startTime = System.currentTimeMillis();
        method.invoke(Main.class, NUMBER_OF_OBJECTS);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Start Time : " + startTime);
        System.out.println("End Time : " + endTime);
        System.out.println("Elapsed Time : " + elapsedTime);
        System.out.println();
        return elapsedTime;
    }

    public static List<Object> singleThreadTest(long nObject) {
        List<Object> list = new ArrayList<>();
        for (long i = 0; i < nObject; i++) {
            list.add(new Object());
        }
        return list;
    }

    public static List<Object> multiThreadTest(long nObject) throws InterruptedException {
        List<Object> result = new ArrayList<>();

        ExecutorService service = Executors.newFixedThreadPool(MAX_THREADS);
        long batch = nObject / MAX_THREADS;
        for (int i = 0; i < MAX_THREADS; i++) {
            result.add(service.submit(() -> singleThreadTest(batch)));
        }

        return result;
    }


}
