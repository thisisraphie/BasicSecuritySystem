import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.stream.Collectors;



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

    public static boolean usernameExists(String username) {
    try {
        HttpURLConnection conn = (HttpURLConnection) new URI(DATABASE_URL).toURL().openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = reader.lines().collect(Collectors.joining());
        reader.close();
        conn.disconnect();

        if (response == null || response.equals("null") || response.isEmpty()) {
            return false;
        }

        return response.contains("\"username\":\"" + username + "\"");

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
    public interface OnUserFetchListener {
        void onSuccess(String username, String hashedPassword);
        void onFailure(String errorMessage);
    }

    public static void getUser(String username, OnUserFetchListener listener) {
    new Thread(() -> {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URI(DATABASE_URL).toURL().openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.lines().collect(Collectors.joining());
            reader.close();
            conn.disconnect();

            if (response == null || response.equals("null") || response.isEmpty()) {
                listener.onFailure("No users found");
                return;
            }

            if (response.contains("\"username\":\"" + username + "\"")) {
                int userIndex = response.indexOf("\"username\":\"" + username + "\"");

                int emailStart = response.lastIndexOf("\"email\":\"", userIndex) + 9;
                int emailEnd = response.indexOf("\"", emailStart);
                String email = response.substring(emailStart, emailEnd);


                int passStart = response.indexOf("\"password\":\"", userIndex) + 12;
                int passEnd = response.indexOf("\"", passStart);
                String hashedPassword = response.substring(passStart, passEnd);

                listener.onSuccess(username, hashedPassword);
            } else {
                listener.onFailure("User not found");
            }

        } catch (Exception e) {
            e.printStackTrace();
            listener.onFailure(e.getMessage());
        }
    }).start();
}
}

