package pt.apolomachado.sip;

import com.google.gson.JsonObject;
import pt.apolomachado.sip.api.GeneralAPI;
import pt.apolomachado.sip.api.SteamInventoryAPI;
import pt.apolomachado.sip.api.SteamMarketAPI;

import java.util.Scanner;

public class Instance {

    protected double totalPrice;

    public double getTotalPrice() {
        return totalPrice;
    }

    public Instance() {
        startApplication();
    }

    protected void startApplication() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Steam Username:");
        String steamUsername = scanner.nextLine();
        System.out.println("AppId (730 for CS:GO):");
        int appId = 730;
        try {
            appId = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid AppId, using default.");
        }
        System.out.println("Context (2 is default):");
        int context = 2;
        try {
            context = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid context, using default.");
        }
        System.out.println("Do you want use proxies? (0 = No, 1 = Yes)");
        int useProxies = 0;
        try {
            useProxies = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input, using 0.");
        }
        if(useProxies == 1) {
            System.out.println("[INFO] Loading proxies...");
            GeneralAPI.loadProxies();
        }
        JsonObject object = SteamInventoryAPI.getInventory(steamUsername, appId, context);
        try {
            startProcess(object);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected synchronized void startProcess(JsonObject object) throws InterruptedException {
        boolean done = false;
        while (!done) {
            if(object != null) {
                JsonObject inventory = object.getAsJsonObject("rgInventory");
                JsonObject descriptions = object.getAsJsonObject("rgDescriptions");
                int inventorySize = inventory.size();
                int actualItem = 0;
                for (String key : inventory.keySet()) {
                    JsonObject newObject = inventory.getAsJsonObject(key);
                    String newStr = newObject.get("classid").getAsString() + "_" + newObject.get("instanceid").getAsString();
                    JsonObject jsonObject = descriptions.getAsJsonObject(newStr);
                    String marketHashName = jsonObject.get("market_hash_name").getAsString();
                    String marketable = jsonObject.get("marketable").getAsString();
                    if(marketable.equalsIgnoreCase("1")) {
                        JsonObject marketData = SteamMarketAPI.getPrice(marketHashName, 730, 3);
                        if(marketData != null && marketData.get("lowest_price") != null) {
                            double price = Double.parseDouble(marketData.get("lowest_price").getAsString().replace("€", "").replace(",", "."));
                            totalPrice += price;
                        }
                    }
                    actualItem++;
                    System.out.println(actualItem + "/" + inventorySize);
                    wait(5000);
                }
                done = true;
            }
        }
        System.out.printf("\nTotal inventory price: %.2f €", getTotalPrice());
    }
}