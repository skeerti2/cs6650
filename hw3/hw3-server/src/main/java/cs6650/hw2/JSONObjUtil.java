package cs6650.hw2;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;

import java.nio.charset.StandardCharsets;

public class JSONObjUtil {
    private static Gson gson = new Gson();
    public static boolean sendMessageToQueue(SkierRide message, String QUEUE_NAME, Channel channel) {
        try {
            //create json from valid params
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String skierJSON = gson.toJson(message); // serializes SkierRide Object to Json
            channel.basicPublish("", QUEUE_NAME, null, skierJSON.getBytes(StandardCharsets.UTF_8));
            System.out.println("Added to queue");
            return true;
        } catch (Exception e) {
            //logger.info("Failed to send message to RabbitMQ");
            System.out.println("Failed to send message to RabbitMQ");
            return false;
        }
    }


}