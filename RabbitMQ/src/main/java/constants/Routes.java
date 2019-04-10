package constants;

public final class Routes {

    private static final String elbowRoutingKey = "app.technicians.elbow";
    private static final String kneeRoutingKey = "app.technicians.knee";
    private static final String hipRoutingKey = "app.technicians.hip";
    private static final String doctorPrefix = "app.doctor.";


    public static String getRoutingKey(String examination) {
        switch (examination) {
            case "elbow":
                return Routes.elbowRoutingKey;
            case "knee":
                return Routes.kneeRoutingKey;
            case "hip":
                return Routes.hipRoutingKey;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static String getDoctorRoutingKey(String doctorID) {
        return doctorPrefix + doctorID;
    }
}
