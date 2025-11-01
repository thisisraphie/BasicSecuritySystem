import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.stream.Collectors;

public class FirebaseConnection {
    private static final String DATABASE_URL =
        "https://basicsecuritysystemdb-default-rtdb.asia-southeast1.firebasedatabase.app/users.json";
    
    public static boolean sendData(String email, String username, String password, boolean isAdmin) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URI(DATABASE_URL).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);
            
            String json = String.format(
                "{\"email\":\"%s\",\"username\":\"%s\",\"password\":\"%s\",\"isAdmin\":%b}",
                email, username, password, isAdmin
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
    
    public static boolean sendData(String email, String username, String password) {
        return sendData(email, username, password, false);
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
        void onSuccess(String email, String hashedPassword, boolean isAdmin);
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
                    int objectStart = response.lastIndexOf("{", userIndex);
                    int objectEnd = response.indexOf("}", userIndex);
                    
                    if (objectStart != -1 && objectEnd != -1) {
                        String userJson = response.substring(objectStart, objectEnd + 1);
                        
                        String email = "";
                        int emailStart = userJson.indexOf("\"email\":\"");
                        if (emailStart != -1) {
                            emailStart += 9;
                            int emailEnd = userJson.indexOf("\"", emailStart);
                            email = userJson.substring(emailStart, emailEnd);
                        }
                        
                        int passStart = userJson.indexOf("\"password\":\"") + 12;
                        int passEnd = userJson.indexOf("\"", passStart);
                        String hashedPassword = userJson.substring(passStart, passEnd);
                        
                        boolean isAdmin = false;
                        if (userJson.contains("\"isAdmin\":true")) {
                            isAdmin = true;
                        }
                        
                        listener.onSuccess(email, hashedPassword, isAdmin);
                    } else {
                        listener.onFailure("Invalid user data structure");
                    }
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