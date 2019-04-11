import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.apache.commons.lang3.SerializationUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Admin extends HospitalWorker {
    private String queueName;

    Admin() throws IOException, TimeoutException {
        queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, HOSPITAL_EXCHANGE, "#");
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException, TimeoutException {
        var admin = new Admin();
        var br = new BufferedReader(new InputStreamReader(System.in));

        admin.listenLogs();
        while (true) {
            System.out.println("Insert message:");
            String message = br.readLine();
            admin.sendBroadcast(message);
        }
    }

    private void sendBroadcast(String message) throws IOException {
        var props = new BasicProperties
                .Builder()
                .type(AdminMessage)
                .build();

        channel.basicPublish(LOG_EXCHANGE, "", props, message.getBytes());
    }

    private void listenLogs() throws IOException {
        var adminConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {

                switch (properties.getType()) {
                    case ExaminationResponse: {
                        String message = new String(body);
                        System.out.println("[LOG] Received result: " + message);
                        break;
                    }
                    case OrderExamination: {
                        Message message = SerializationUtils.deserialize(body);
                        System.out.println("[LOG] Received assignment: " + message.examination + " from: " + properties.getReplyTo());
                        break;
                    }
                    default:
                        System.out.println("[LOG] Received unknown message type");
                }
            }
        };
        System.out.println("Listening for logs...");
        this.channel.basicConsume(queueName, true, adminConsumer);
    }
}
