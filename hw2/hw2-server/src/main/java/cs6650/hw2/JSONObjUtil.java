package cs6650.hw2;

import com.rabbitmq.client.Channel;

public class JSONObjUtil {
    public static boolean sendMessageToQueue(SkierRide message, String QUEUE_NAME, Channel channel) {
        try {
            //create json from valid params
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println("Added to queue");
            return true;
        } catch (Exception e) {
            //logger.info("Failed to send message to RabbitMQ");
            System.out.println("Failed to send message to RabbitMQ");
            return false;
        }
    }


}