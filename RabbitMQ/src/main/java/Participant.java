import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

abstract class Participant {
    protected Channel channel;
    protected final static String HOSPITAL_EXCHANGE = "MAIN_EXCHANGE";
    protected final static String LOG_EXCHANGE = "LOG_EXCHANGE";

    Participant() throws IOException, TimeoutException {
        this.channel = establishConnection();
    }

    private Channel establishConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(HOSPITAL_EXCHANGE, BuiltinExchangeType.TOPIC);
        channel.exchangeDeclare(LOG_EXCHANGE, BuiltinExchangeType.FANOUT);

        return channel;
    }
}
