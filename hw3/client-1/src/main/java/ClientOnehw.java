import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CountDownLatch;


import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ClientOnehw {
    private static SkiersApi skiersApi = new SkiersApi();
    private static AtomicInteger successfulRequests = new AtomicInteger();
    private static AtomicInteger unsuccessfulRequests = new AtomicInteger();
    private static AtomicInteger totalRequests = new AtomicInteger();



    private static int getRandomLift(int numLifts){
        int boundedRandomValue = ThreadLocalRandom.current().nextInt(1, numLifts+1);
        return boundedRandomValue;
    }

    private static int getRandomTime(int timeStart, int timeEnd){
        int randomTime = ThreadLocalRandom.current().nextInt(timeStart, timeEnd+1);
        return randomTime;
    }

    private static int getWaitTime(){
        int maxWaitTime = 10;
        int randomWaitTime = ThreadLocalRandom.current().nextInt(0, maxWaitTime+1);
        return randomWaitTime;
    }

    private static int getRandomSkierId(int startSkierId, int endSkierId){
        int randomSkierId = ThreadLocalRandom.current().nextInt(startSkierId, endSkierId+1);
        return randomSkierId;
    }

    public static void doPhaseTwo(int numSkiers, int numRuns, int numThreads, int numLifts) throws InterruptedException {
        CountDownLatch  completed = new CountDownLatch(numThreads);
        int threadCount = 0;
        int skierFactor = numSkiers/numThreads;
        while(threadCount < numThreads){
            int startSkierId = threadCount*skierFactor + 1;
            int endSkierId = (threadCount+1)*skierFactor;
            Runnable thread = () -> {
                ApiResponse res = null;
                try {
                    int runCount = 0;
                    int phaseTwoRequestsPerThread = (int) ((numRuns*0.6)*(numSkiers/numThreads));
                    int liftID = getRandomLift(numLifts);
                    int randomTime = getRandomTime(91, 360);
                    while(runCount < phaseTwoRequestsPerThread){
                        try {
                            LiftRide liftRide = new LiftRide().liftID(liftID).time(randomTime).waitTime(getWaitTime());
                            res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
                            runCount++;
                            totalRequests.incrementAndGet();
                            int responseCode = res.getStatusCode();
                            if (responseCode == 200) {
                                successfulRequests.incrementAndGet();
                            } else {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {}
//                                int counter = 5;
//                                while (counter > 0) {
//                                    System.out.println("bad response, trying again");
//                                    totalRequests.incrementAndGet();
//                                    res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
//                                    if (res.getStatusCode() >= 400) {
//                                        unsuccessfulRequests.incrementAndGet();
//                                        counter--;
//                                    } else {
//                                        successfulRequests.incrementAndGet();
//                                        break;
//                                    }
//                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                completed.countDown();
            };
            new Thread(thread).start();
            threadCount++;
        }
        while(true){
            if(completed.getCount() <= (int) ((1 - 0.2)*numThreads)){
                doPhaseThree(numSkiers, numRuns, numThreads, numLifts);
                break;
            }
        }
        completed.await();
    }

    public static void doPhaseThree(int numSkiers, int numRuns, int numThreads, int numLifts) throws InterruptedException {
        System.out.println("Phase three started");
        int phaseThreeThreads = (int) (0.1*numThreads);
        CountDownLatch  completed = new CountDownLatch(phaseThreeThreads);
        int threadCount = 0;
        int skierFactor = numSkiers/(numThreads/4);
        while(threadCount < phaseThreeThreads){
            int startSkierId = threadCount*skierFactor + 1;
            int endSkierId = (threadCount+1)*skierFactor;

            Runnable thread = () -> {
                ApiResponse res = null;
                try {
                    int runCount = 0;
                    int phaseThreeRequestsPerThread = (int) (numRuns*0.1);
                    int liftID = getRandomLift(numLifts);
                    int randomTime = getRandomTime(361, 420);
                    while(runCount < phaseThreeRequestsPerThread) {
                        LiftRide liftRide = new LiftRide().liftID(liftID).time(randomTime).waitTime(getWaitTime());
                        res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));

                        runCount++;
                        totalRequests.incrementAndGet();
                        int responseCode = res.getStatusCode();
                        if(responseCode == 200){
                            successfulRequests.incrementAndGet();
                        }else{
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {}
//                            int counter = 5;
//                            while(counter > 0){
//                                System.out.println("bad response, trying again");
//                                totalRequests.incrementAndGet();
//                                res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
//                                if(res.getStatusCode() >= 400){
//                                    unsuccessfulRequests.incrementAndGet();
//                                    counter--;
//                                }else{
//                                    successfulRequests.incrementAndGet();
//                                    break;
//                                }
//                            }
                        }
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
                completed.countDown();
            };
            new Thread(thread).start();
            threadCount++;
        }
        completed.await();
        System.out.println("Phase three ends");
    }




    public static void doPhaseOne(int phaseOneThreads, int numSkiers, int numRuns, int numThreads, int numLifts) throws InterruptedException {
        CountDownLatch  completed = new CountDownLatch(phaseOneThreads);
        int threadCount = 0;
        int skierFactor = numSkiers/(numThreads/4);
        while(threadCount < phaseOneThreads){
            int startSkierId = threadCount*skierFactor + 1;
            int endSkierId = (threadCount+1)*skierFactor;
            Runnable thread = () -> {
                ApiResponse res = null;
                try {
                    int runCount = 0;
                    int phaseOneRequestsPerThread = (int) ((numRuns*0.2)*(numSkiers/(numThreads/4)));
                    int liftID = getRandomLift(numLifts);
                    int randomTime = getRandomTime(0, 90);
                    while(runCount < phaseOneRequestsPerThread) {
                        LiftRide liftRide = new LiftRide().liftID(liftID).time(randomTime).waitTime(getWaitTime());
                        res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
                        runCount++;
                        totalRequests.incrementAndGet();
                        int responseCode = res.getStatusCode();
                        if(responseCode == 200){
                            successfulRequests.incrementAndGet();
                        }else{
                            try {
                                Thread.sleep(500);
                                } catch (InterruptedException e) {}
//                            int counter = 5;
//                            while(counter > 0){
//                                System.out.println("bad response, trying again");
//                                totalRequests.incrementAndGet();
//                                res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
//                                if(res.getStatusCode() >= 400){
//                                    unsuccessfulRequests.incrementAndGet();
//                                    counter--;
//                                }else{
//                                    successfulRequests.incrementAndGet();
//                                    break;
//                                }
//                            }
                        }
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
                completed.countDown();
            };
            new Thread(thread).start();
            threadCount++;
        }

        while(true){
            if(completed.getCount() <= (int) ((1 - 0.2)*phaseOneThreads)){
                System.out.println("calling phase two");
                doPhaseTwo(numSkiers, numRuns, numThreads, numLifts);
                break;
            }
        }
        completed.await();
    }

    private static double getLatency() throws ApiException {
        long startTime;
        long endTime;
        long timeDiffSum= 0L;
        int numRequests = 10000;
        long theoriticalThroughput = 0L;
        int counter = 1;
               while(counter <= numRequests){
                   startTime = System.currentTimeMillis();
                   LiftRide liftRide = new LiftRide().liftID(10).time(8).waitTime(10);
                   try{
                       skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(1, 100000));
                   }catch (Exception e){
                       continue;
                   }
                   endTime = System.currentTimeMillis();
                   counter++;
                    timeDiffSum += (endTime - startTime);
               }
        theoriticalThroughput = numRequests / (timeDiffSum/1000);
        return theoriticalThroughput;
    }
    public static void main(String[] args) throws ApiException, InterruptedException, IOException {
//        skiersApi.getApiClient().setBasePath("http://localhost:8080/hw2_war_exploded/");
//        skiersApi.getApiClient().setBasePath("http://ec2-user@ec2-user@ec2-52-27-166-51.us-west-2.compute.amazonaws.com:8080/hw2_war/");
        skiersApi.getApiClient().setBasePath("http://hw2-load-balancer-test-2025416995.us-west-2.elb.amazonaws.com/hw2_war/");
        skiersApi.getApiClient().setConnectTimeout(1*60*1000);
        Scanner in = new Scanner(System.in);

        System.out.println("Enter maximum number of threads to run: ");
        int numThreads = in.nextInt();
        while(numThreads > 1024){
            System.out.println("Enter number of threads less than or equal to 1024: ");
            numThreads = in.nextInt();
        }

        System.out.println("Enter the number of skiers ");
        int numSkiers = in.nextInt();
        while(numSkiers > 100000){
            System.out.println("Number of skiers can only be 100000 max: Enter again ");
            numSkiers = in.nextInt();
        }

        // Doing this to cater to the enter key press from the previous input
        in.nextLine();

        int numLifts = 40;
        while(true) {
            System.out.println("Enter number of ski lifts hit enter to assign default value 40 ");
            String inputStr = in.nextLine();
            if (inputStr.equals("")) {
                System.out.println("Assigning default value of 40");
                break;
            } else {
                int inputInt = Integer.parseInt(inputStr);
                if (inputInt < 5 || inputInt > 60) {
                    System.out.println("Error!");
                } else {
                    numLifts = inputInt;
                    break;
                }
            }
        }

        System.out.println("Enter average ski lifts each skier rides each day: ");
        int numRuns = in.nextInt();

        System.out.println("Enter server port address: ");
        int portAddress = in.nextInt();

        // closing scanner
        int phaseOneThreadCount = (int) (numThreads/4);
        in.close();

        long startTimestamp = System.currentTimeMillis();
        doPhaseOne(phaseOneThreadCount, numSkiers, numRuns, numThreads, numLifts);
        long endTimeStamp = System.currentTimeMillis();
        long wallTime = (endTimeStamp - startTimestamp)/1000;
        long throughPut = totalRequests.get()/(wallTime);
        System.out.println("Total requests: "+ totalRequests);
        System.out.println("Number of successful requests sent: " + successfulRequests);
        System.out.println("Number of failed requests sent: " + unsuccessfulRequests);
        System.out.println("wall time is: " + wallTime + " seconds");
        System.out.println("Throughput is: " + throughPut);
        System.out.println("Expected throughput for 10000 requests: " + getLatency());
    }
}


