import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import constants.Routes;
import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Doctor extends Participant {

    private String doctorID;
    private String callbackQueue;

    private Doctor(String doctorID) throws IOException, TimeoutException {
        this.doctorID = doctorID;

        callbackQueue = channel.queueDeclare().getQueue();
        channel.queueBind(callbackQueue, HOSPITAL_EXCHANGE, Routes.getDoctorRoutingKey(doctorID));

    }

    public static void main(String[] args) throws IOException, TimeoutException {
        var doctor = new Doctor(args[0]);

        doctor.orderExamination("elbow", "Joe");
    }

    private void orderExamination(String examination, String patientName) throws IOException {
        orderExaminationCallback();

        var message = SerializationUtils.serialize(new Message(examination, patientName));
        var routingKey = Routes.getRoutingKey(examination);
        var props = new BasicProperties
                .Builder()
                .replyTo(Routes.getDoctorRoutingKey(doctorID))
                .build();

        channel.basicPublish(HOSPITAL_EXCHANGE, routingKey, props, message);
        System.out.println("Send request");
    }

    private void orderExaminationCallback() throws IOException {
        var consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
                String message = new String(body);
                System.out.println("Received result: " + message);
            }
        };
        channel.basicConsume(this.callbackQueue, true, consumer);
    }


}
