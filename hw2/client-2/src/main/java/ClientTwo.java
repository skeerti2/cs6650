import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CountDownLatch;


import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientTwo {
    private static SkiersApi skiersApi = new SkiersApi();
    private static AtomicInteger successfulRequests = new AtomicInteger();
    private static AtomicInteger unsuccessfulRequests = new AtomicInteger();
    private static AtomicInteger totalRequests = new AtomicInteger();
    private static CopyOnWriteArrayList<String[]> listData = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<Long> latencyList = new CopyOnWriteArrayList<>();

    
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

    private static synchronized void addToArrayList(long startTime, long latency, int responseCode){
        if(listData.size() == 0){
            listData.add(new String[]
                    { "Start time", "Request Type", "Latency", "Response Code"});
        }else{
            listData.add(new String[]
                    { String.valueOf(startTime), "POST", String.valueOf(latency), String.valueOf(responseCode)});
        }
        latencyList.add(latency);
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
                            long timeStampBeforePost = System.currentTimeMillis();
                            res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
                            long timeStampAfterPost = System.currentTimeMillis();                            runCount++;
                            totalRequests.incrementAndGet();
                            int responseCode = res.getStatusCode();
                            if (responseCode == 200) {
                                successfulRequests.incrementAndGet();
                            } else {
                                int counter = 5;
                                while (counter > 0) {
                                    System.out.println("bad response, trying again");
                                    totalRequests.incrementAndGet();
                                    timeStampBeforePost = System.currentTimeMillis();
                                    res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
                                    timeStampAfterPost = System.currentTimeMillis();                                    if (res.getStatusCode() >= 400) {
                                        unsuccessfulRequests.incrementAndGet();
                                        counter--;
                                    } else {
                                        successfulRequests.incrementAndGet();
                                        break;
                                    }
                                }
                            }
                            addToArrayList(timeStampBeforePost, timeStampAfterPost-timeStampBeforePost, responseCode);
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
                        long timeStampBeforePost = System.currentTimeMillis();
                        res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
                        long timeStampAfterPost = System.currentTimeMillis();                        runCount++;
                        totalRequests.incrementAndGet();
                        int responseCode = res.getStatusCode();
                        if(responseCode == 200){
                            successfulRequests.incrementAndGet();
                        }else{
                            int counter = 5;
                            while(counter > 0){
                                System.out.println("bad response, trying again");
                                totalRequests.incrementAndGet();
                                timeStampBeforePost = System.currentTimeMillis();
                                res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
                                timeStampAfterPost = System.currentTimeMillis();                                if(res.getStatusCode() >= 400){
                                    unsuccessfulRequests.incrementAndGet();
                                    counter--;
                                }else{
                                    successfulRequests.incrementAndGet();
                                    break;
                                }
                            }
                        }
                        addToArrayList(timeStampBeforePost, timeStampAfterPost-timeStampBeforePost, responseCode);
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
                        long timeStampBeforePost = System.currentTimeMillis();
                        res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
                        long timeStampAfterPost = System.currentTimeMillis();
                        runCount++;
                        totalRequests.incrementAndGet();
                        int responseCode = res.getStatusCode();
                        if(responseCode == 200){
                            successfulRequests.incrementAndGet();
                        }else{
                            int counter = 5;
                            while(counter > 0){
                                System.out.println("bad response, trying again");
                                totalRequests.incrementAndGet();
                                timeStampBeforePost = System.currentTimeMillis();
                                res = skiersApi.writeNewLiftRideWithHttpInfo(liftRide, 3, "34", "4", getRandomSkierId(startSkierId, endSkierId));
                                timeStampAfterPost = System.currentTimeMillis();
                                if(res.getStatusCode() >= 400){
                                    unsuccessfulRequests.incrementAndGet();
                                    counter--;
                                }else{
                                    successfulRequests.incrementAndGet();
                                    break;
                                }
                            }
                        }
                        addToArrayList(timeStampBeforePost, timeStampAfterPost-timeStampBeforePost, responseCode);
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

    public static String convertToCSV(String[] data) {
        return Stream.of(data)
                .collect(Collectors.joining(","));
    }


    public static void writeStringData() throws IOException {
        File csvOutputFile = new File("outputCSV.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            listData.stream()
                    .map(x -> convertToCSV(x))
                    .forEach(pw::println);
        }
    }
    public static void main(String[] args) throws ApiException, InterruptedException, IOException {
//        skiersApi.getApiClient().setBasePath("http://localhost:8080/hw1_server_war_exploded/");
        skiersApi.getApiClient().setBasePath("http://ec2-34-211-35-199.us-west-2.compute.amazonaws.com:8080/hw1-server_war/");
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
        writeStringData();
        long sumLatency = 0;
        for(int i=0; i<latencyList.size(); i++){
            sumLatency += latencyList.get(i);
        }
        long meanLatency = sumLatency/latencyList.size();
        System.out.println("Mean response time is: " + meanLatency + " Milliseconds");

        Object[] a = latencyList.toArray();
        Arrays.sort(a);
        for (int i = 0; i < a.length; i++) {
            latencyList.set(i, (Long) a[i]);
        }
        Median median = new Median();
        Percentile percentile = new Percentile();
        double[] latencyArray = latencyList.stream().mapToDouble(x -> x).toArray();
        double medianValue = median.evaluate(latencyArray);
        double percentile99Value = percentile.evaluate(latencyArray, 99);
        System.out.println("Median is: " + medianValue);
        System.out.println("99th Percentile is: " + percentile99Value);
        System.out.println("Min latency is: " + a[0]);
        System.out.println("Max latency is: " + a[a.length - 1]);


    }
}


