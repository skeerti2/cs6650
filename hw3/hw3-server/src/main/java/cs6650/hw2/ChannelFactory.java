package cs6650.hw2;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ChannelFactory extends BasePoolableObjectFactory {
    private final Connection conn;

    public ChannelFactory() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        //tomcat server establishes connection wth RMQ server at below address
        factory.setHost("ec2-34-211-48-98.us-west-2.compute.amazonaws.com");
        factory.setUsername("test");
        factory.setPassword("test");
        conn = factory.newConnection();
    }

    @Override
    public Channel makeObject() throws Exception {
        Channel channel = conn.createChannel();
        return channel;
    }
}
