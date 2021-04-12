package pt.apolomachado.sip.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Scanner;

public class SteamInventoryAPI {

    private static final String url = "https://steamcommunity.com/id/%username/inventory/json/%appId/%context";
    private static final JsonParser jsonParser = new JsonParser();

    public static JsonObject getInventory(String username, int appId, int context) {
        JsonObject jsonObject = null;
        boolean success = false;
        while(!success) {
            try {
                URL urlObject = new URL(url.replace("%username", username).replace("%appId", String.valueOf(appId)).replace("%context", String.valueOf(context)).replace(" ", "%20"));

                HttpURLConnection httpURLConnection = (HttpURLConnection) urlObject.openConnection();
                if(GeneralAPI.proxiesEnabled()) {
                    Proxy proxy = GeneralAPI.choiceProxy();
                    httpURLConnection = (HttpURLConnection) urlObject.openConnection(proxy);
                    System.out.println("[SteamInventoryAPI] Retrieving data with proxy: " + proxy.address().toString());
                }
                httpURLConnection.connect();

                int responseCode = httpURLConnection.getResponseCode();
                if(responseCode == 429) {
                    System.out.println("[Error] Your app has made too many requests.");
                    return null;
                } else if(responseCode == 400) {
                    System.out.println("[Error] Bad Request.");
                    return null;
                }

                Scanner scanner = new Scanner(urlObject.openStream());
                StringBuilder json = null;
                while (scanner.hasNextLine()) {
                    if(json != null) {
                        json.append(scanner.nextLine());
                    } else {
                        json = new StringBuilder(scanner.nextLine());
                    }
                }

                if (json != null) {
                    jsonObject = (JsonObject) jsonParser.parse(json.toString());
                    success = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
}