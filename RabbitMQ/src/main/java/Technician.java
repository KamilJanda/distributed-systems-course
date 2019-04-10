import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import constants.Routes;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

class Technician extends Participant {
    private String queueName;

    Technician(List<String> skills) throws IOException, TimeoutException {
        queueName = channel.queueDeclare().getQueue();
        for (String skill : skills) {
            channel.queueBind(queueName, HOSPITAL_EXCHANGE, Routes.getRoutingKey(skill));
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        var technician = new Technician(Arrays.asList(args));
        technician.handleRequest();
    }


    private void handleRequest() throws IOException {
        var consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
                Message message = SerializationUtils.deserialize(body);
                System.out.println("Received assignment: " + message.examination + " from: " + properties.getReplyTo());
                var response = message.patientName + " " + message.examination + " done";
                examinationResponse(response, properties.getReplyTo());
            }
        };
        System.out.println("Waiting for messages...");
        channel.basicConsume(this.queueName, true, consumer);
    }

    private void examinationResponse(String message, String receiver) throws IOException {
        channel.basicPublish(HOSPITAL_EXCHANGE, receiver, null, message.getBytes());

    }
}
