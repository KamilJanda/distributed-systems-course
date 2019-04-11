import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

abstract class HospitalWorker {
    protected Channel channel;
    protected String logQueue;
    protected final static String HOSPITAL_EXCHANGE = "MAIN_EXCHANGE";
    protected final static String LOG_EXCHANGE = "LOG_EXCHANGE";
    protected final static String OrderExamination = "OrderExamination";
    protected final static String ExaminationResponse = "ExaminationResponse";
    protected final static String AdminMessage = "AdminMessage";


    HospitalWorker() throws IOException, TimeoutException {
        this.channel = establishConnection();

        logQueue = channel.queueDeclare().getQueue();
        channel.queueBind(logQueue, LOG_EXCHANGE, "");
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

    protected void listenAdmin() throws IOException {
        var adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties props, byte[] body) throws IOException {
                System.out.println("Info from admin: " + new String(body, "UTF-8"));
            }
        };
        this.channel.basicConsume(this.logQueue, true, adminConsumer);
    }
}
