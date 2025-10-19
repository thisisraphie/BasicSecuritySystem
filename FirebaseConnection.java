import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;


public class FirebaseConnection {

    private static final String DATABASE_URL =
        "https://basicsecuritysystemdb-default-rtdb.asia-southeast1.firebasedatabase.app/users.json";


    public static boolean sendData(String email, String username, String password) {

        try {

            HttpURLConnection conn = (HttpURLConnection) new URI(DATABASE_URL).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            String json = String.format(
                "{\"email\":\"%s\",\"username\":\"%s\",\"password\":\"%s\"}",
                email, username, password
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("utf-8"));
            }

            int code = conn.getResponseCode();
            conn.disconnect();

            return code / 100 == 2;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }
    }

}
