package cs6650.consumer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Recv {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("ec2-54-218-107-33.us-west-2.compute.amazonaws.com");
        factory.setUsername("test");
        factory.setPassword("test");
        final Connection connection = factory.newConnection();
        final int numThreads = 50;
        ConcurrentHashMap hashmap = new ConcurrentHashMap();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final Channel channel = connection.createChannel();
                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                    // max one message per receiver
                    channel.basicQos(1);
                    System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        SkierRide skierRideObj = SkierRide.fromBytes(delivery.getBody());
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        System.out.println( "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + skierRideObj.toString() + "'");
                    };
                    // process messages
                    channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
                    //add element to hashmap

                    // hashmap.put()
                } catch (IOException ex) {
                    Logger.getLogger(Recv.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        for(int i=0; i < numThreads; i++){
            Thread thread = new Thread(runnable);
            thread.start();
        }

    }
}