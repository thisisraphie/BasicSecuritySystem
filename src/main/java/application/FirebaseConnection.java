package application;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class FirebaseConnection {

    private static final String DATABASE_URL =
        "https://basicsecuritysystemdb-default-rtdb.asia-southeast1.firebasedatabase.app/users.json";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static boolean sendData(String email, String username, String password, boolean isAdmin) {
        try {
            String json = String.format(
                "{\"email\":\"%s\",\"username\":\"%s\",\"password\":\"%s\",\"isAdmin\":%b}",
                email, username, password, isAdmin
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DATABASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            return response.statusCode() / 100 == 2;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean usernameExists(String username) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DATABASE_URL))
                .GET()
                .build();

            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            String responseBody = response.body();
            if (responseBody == null || responseBody.equals("null") || responseBody.isEmpty()) {
                return false;
            }

            return responseBody.contains("\"username\":\"" + username + "\"");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface OnUserFetchListener {
        void onSuccess(String username, String hashedPassword, boolean isAdmin);
        void onFailure(String errorMessage);
    }

    public static void getUser(String username, OnUserFetchListener listener) {
        httpClient.sendAsync(
            HttpRequest.newBuilder()
                .uri(URI.create(DATABASE_URL))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        ).thenAccept(response -> {
            String responseBody = response.body();
            if (responseBody == null || responseBody.equals("null") || responseBody.isEmpty()) {
                listener.onFailure("No users found");
                return;
            }

            if (responseBody.contains("\"username\":\"" + username + "\"")) {
                int userIndex = responseBody.indexOf("\"username\":\"" + username + "\"");
                int objectStart = responseBody.lastIndexOf("{", userIndex);
                int objectEnd = responseBody.indexOf("}", userIndex);

                if (objectStart != -1 && objectEnd != -1) {
                    String userJson = responseBody.substring(objectStart, objectEnd + 1);

                    String hashedPassword = extractValue(userJson, "password");
                    boolean isAdmin = userJson.contains("\"isAdmin\":true");

                    listener.onSuccess(username, hashedPassword, isAdmin);
                } else {
                    listener.onFailure("Invalid user data structure");
                }
            } else {
                listener.onFailure("User not found");
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            listener.onFailure(e.getMessage());
            return null;
        });
    }

    public static boolean updateAdminStatus(String username, boolean isAdmin) {
    try {
        // First, find the user's key in Firebase
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(DATABASE_URL))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        if (body == null || body.equals("null")) return false;

        String userKey = extractUserKey(body, username);
        if (userKey == null) return false;

        // Create JSON for update
        String json = String.format("{\"isAdmin\":%b}", isAdmin);
        String updateUrl = DATABASE_URL.replace(".json", "/" + userKey + ".json");

        HttpRequest patchRequest = HttpRequest.newBuilder()
                .uri(URI.create(updateUrl))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> patchResponse = httpClient.send(patchRequest, HttpResponse.BodyHandlers.ofString());
        return patchResponse.statusCode() / 100 == 2;

    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

// Helper to extract the Firebase key for a username
private static String extractUserKey(String json, String username) {
    // Firebase RTDB JSON structure: {"userKey": {userData}, ...}
    for (String entry : json.split("},")) {
        if (entry.contains("\"username\":\"" + username + "\"")) {
            int keyStart = entry.indexOf("{");
            if (keyStart == -1) continue;
            String sub = entry.substring(0, keyStart).trim();
            sub = sub.replaceAll("[\"{}:]", ""); // remove quotes and braces
            return sub;
        }
    }
    return null;
}


    private static String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) return "";
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }
}
