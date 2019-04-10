import java.io.Serializable;

class Message implements Serializable {
    String examination;
    String patientName;

    public Message(String examination, String patientName) {
        this.examination = examination;
        this.patientName = patientName;
    }
}
