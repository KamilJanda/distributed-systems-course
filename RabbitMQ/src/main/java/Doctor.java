import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import constants.Routes;
import org.apache.commons.lang3.SerializationUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Doctor extends HospitalWorker {

    private String doctorID;
    private String callbackQueue;

    private Doctor(String doctorID) throws IOException, TimeoutException {
        this.doctorID = doctorID;

        callbackQueue = channel.queueDeclare().getQueue();
        channel.queueBind(callbackQueue, HOSPITAL_EXCHANGE, Routes.getDoctorRoutingKey(doctorID));
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        var doctor = new Doctor(args[0]);

        doctor.listenAdmin();
        assignExamination(doctor);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private static void assignExamination(Doctor doctor) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Insert examination type:");
            String examinationType = br.readLine();
            System.out.println("Insert patient name");
            String patientName = br.readLine();
            doctor.orderExamination(examinationType, patientName);
        }
    }

    private void orderExamination(String examination, String patientName) throws IOException {
        orderExaminationCallback();

        var message = SerializationUtils.serialize(new Message(examination, patientName));
        var routingKey = Routes.getRoutingKey(examination);
        var props = new BasicProperties
                .Builder()
                .replyTo(Routes.getDoctorRoutingKey(doctorID))
                .type(OrderExamination)
                .build();

        channel.basicPublish(HOSPITAL_EXCHANGE, routingKey, props, message);
        System.out.println("Send request");
    }

    private void orderExaminationCallback() throws IOException {
        var consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {

                switch (properties.getType()) {
                    case ExaminationResponse: {
                        String message = new String(body);
                        System.out.println("Received result: " + message);
                        break;
                    }
                    case AdminMessage: {
                        String message = new String(body);
                        System.out.println("[ADMIN INFO] Message from admin: " + message);
                        break;
                    }
                }
            }
        };
        channel.basicConsume(this.callbackQueue, true, consumer);
        channel.basicConsume(this.logQueue, true, consumer);
    }


}
