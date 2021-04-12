package pt.apolomachado.sip.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Scanner;

public class SteamMarketAPI {

    /**
     * Known currency types:
     *  1 -> USD
     *  3 -> EUR
     **/
    private static final String url = "https://steamcommunity.com/market/priceoverview/?appid=%appId&currency=%currency&market_hash_name=%marketHashName";
    private static final JsonParser jsonParser = new JsonParser();

    public static JsonObject getPrice(String marketHashName, int appId, int currency) {
        JsonObject jsonObject = null;
        boolean success = false;
        while(!success) {
            try {
                URL urlObject = new URL(url.replace("%marketHashName", marketHashName).replace("%appId", String.valueOf(appId)).replace("%currency", String.valueOf(currency)).replace(" ", "%20"));
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlObject.openConnection();
                if(GeneralAPI.proxiesEnabled()) {
                    Proxy proxy = GeneralAPI.choiceProxy();
                    System.out.println("[SteamMarketAPI] Retrieving data with proxy: " + proxy.address());
                    httpURLConnection = (HttpURLConnection) urlObject.openConnection(proxy);
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
                    if(jsonObject.get("success").getAsString().equalsIgnoreCase("false")) {
                        success = true;
                        return null;
                    }
                    success = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
}