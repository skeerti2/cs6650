package resortMS;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResortReceiver {
    private final static String QUEUE_NAME = "resortQueue";

    public static synchronized void addElementToDatabase(Jedis jedis, SkierRide skierRideObj){
        String jsonList = jedis.get(Integer.toString(skierRideObj.getSkierID()));
        Gson gson = new Gson();
        List<SkierRide> skierRideList = gson.fromJson(jsonList, List.class);
        skierRideList.add(skierRideObj);
        jedis.set(Integer.toString(skierRideObj.getSkierID()), gson.toJson(skierRideList));
    }

    public static synchronized void createElementInDatabase(Jedis jedis, SkierRide skierRideObj){
        List<SkierRide> skierRideList = new ArrayList<SkierRide>();
        skierRideList.add(skierRideObj);
        Gson gson = new Gson();
        jedis.set(Integer.toString(skierRideObj.getSkierID()), gson.toJson(skierRideList));
    }

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("ec2-34-211-48-98.us-west-2.compute.amazonaws.com");
        factory.setUsername("test");
        factory.setPassword("test");
        final Connection connection = factory.newConnection();
        final int numThreads = 50;
        JedisPoolConfiguration jedisPoolConfiguration = new JedisPoolConfiguration();
        JedisPool jedisPool = new JedisPool(jedisPoolConfiguration.getPoolConfig(),"54.218.24.116", 6379, 10000);
        //final Jedis subscriberJedis = jedisPool.getResource();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final Channel channel = connection.createChannel();
                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                    // max one message per receiver
                    channel.basicQos(1);
                    System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");
                    //SkierRide skierRideObj = null;
                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        SkierRide skierRideObj = SkierRide.fromBytes(delivery.getBody());
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + skierRideObj.toString() + "'");
                        try(Jedis jedis = jedisPool.getResource()){
                            if(jedis.exists(Integer.toString(skierRideObj.getSkierID())))
                            {
                                addElementToDatabase(jedis, skierRideObj);
                            } else {
                                createElementInDatabase(jedis, skierRideObj);
                            }
                            System.out.println(jedis.get(Integer.toString(skierRideObj.getSkierID())));
                            jedisPool.returnResource(jedis);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    };
                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
                } catch (IOException ex) {
                    Logger.getLogger(ResortReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        for(int i=0; i < numThreads; i++){
            Thread thread = new Thread(runnable);
            thread.start();
        }

        //jedisPool.close();
        System.out.println("DONE");
    }
}