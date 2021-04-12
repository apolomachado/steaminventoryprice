package pt.apolomachado.sip.panels;

import com.google.gson.JsonObject;
import pt.apolomachado.sip.api.GeneralAPI;
import pt.apolomachado.sip.api.SteamInventoryAPI;
import pt.apolomachado.sip.api.SteamMarketAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class StartPanel extends JFrame {

    public StartPanel() {
        setTitle("Steam Inventory Price");
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 10, 10);

        JLabel infoLabel = new JLabel("This program is frozen while calculating the price of the items in your inventory, you can run it from the console to receive more information about the process.");
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(infoLabel, constraints);

        JLabel labelUsername = new JLabel("Steam username");
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(labelUsername, constraints);

        JTextField usernameField = new JTextField(30);
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(usernameField, constraints);

        JLabel labelAppId = new JLabel("App ID");
        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(labelAppId, constraints);

        JTextField appIdField = new JTextField(5);
        constraints.gridx = 0;
        constraints.gridy = 4;
        panel.add(appIdField, constraints);

        JLabel contextLabel = new JLabel("Context");
        constraints.gridx = 0;
        constraints.gridy = 5;
        panel.add(contextLabel, constraints);

        JTextField contextField = new JTextField(2);
        constraints.gridx = 0;
        constraints.gridy = 6;
        panel.add(contextField, constraints);

        JLabel proxiesLabel = new JLabel("Use proxies?");
        constraints.gridx = 0;
        constraints.gridy = 7;
        panel.add(proxiesLabel, constraints);

        JCheckBox proxies = new JCheckBox();
        constraints.gridx = 0;
        constraints.gridy = 8;
        panel.add(proxies, constraints);

        JLabel statusLabel = new JLabel("Status:");
        constraints.gridx = 0;
        constraints.gridy = 9;
        panel.add(statusLabel, constraints);

        JLabel statusInfoLabel = new JLabel("");
        constraints.gridx = 0;
        constraints.gridy = 10;
        panel.add(statusInfoLabel, constraints);

        JButton run = new JButton("Get inventory's price");
        ActionListener actionListener = e -> {
            run.setEnabled(false);
            String steamUsername = usernameField.getText();
            String appIdStr = appIdField.getText();
            String contextStr = contextField.getText();
            boolean usingProxies = proxies.isSelected();
            usernameField.hide();
            usernameField.setEnabled(false);
            contextField.hide();
            appIdField.hide();
            appIdField.setEnabled(false);
            contextField.setEnabled(false);
            proxies.hide();
            labelUsername.hide();
            labelAppId.hide();
            contextLabel.hide();
            proxiesLabel.hide();
            proxies.setEnabled(false);
            int appId = 730, context = 2;
            try {
                appId = Integer.parseInt(appIdStr);
            } catch (NumberFormatException exception) {
                GeneralAPI.currentLog = "Invalid AppId, using default.";
                statusInfoLabel.setText(GeneralAPI.currentLog);
            }
            try {
                context = Integer.parseInt(contextStr);
            } catch (NumberFormatException exception) {
                GeneralAPI.currentLog = "Invalid context, using default.";
                statusInfoLabel.setText(GeneralAPI.currentLog);
            }
            if(usingProxies) {
                GeneralAPI.currentLog = "Loading proxies...";
                statusInfoLabel.setText(GeneralAPI.currentLog);
                GeneralAPI.loadProxies();
            }
            JsonObject object = SteamInventoryAPI.getInventoryJsonObject(steamUsername, appId, context);
            if(object != null) {
                JsonObject inventory = object.getAsJsonObject("rgInventory");
                JsonObject descriptions = object.getAsJsonObject("rgDescriptions");
                int actualItem = 0;
                double totalPrice = 0;
                try {
                    for(String key : inventory.keySet()) {
                        JsonObject newObject = inventory.getAsJsonObject(key);
                        String newStr = newObject.get("classid").getAsString() + "_" + newObject.get("instanceid").getAsString();
                        JsonObject jsonObject = descriptions.getAsJsonObject(newStr);
                        String marketHashName = jsonObject.get("market_hash_name").getAsString();
                        String marketable = jsonObject.get("marketable").getAsString();
                        if(marketable.equalsIgnoreCase("1")) {
                            JsonObject marketData = SteamMarketAPI.getMarketItemObject(marketHashName, 730, 3);
                            if(marketData != null && marketData.get("lowest_price") != null) {
                                double price = Double.parseDouble(marketData.get("lowest_price").getAsString().replace("€", "").replace(",", "."));
                                System.out.println(price);
                                totalPrice += price;
                            }
                        }
                        actualItem++;
                        GeneralAPI.currentLog = actualItem + "/" + inventory.size();
                        System.out.println(GeneralAPI.currentLog);
                        long currentTime = System.currentTimeMillis();
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        while (System.currentTimeMillis() < currentTime + 5000) {
                            // Ignored
                        }
                    }
                    GeneralAPI.currentLog = String.format("Total inventory's price: %.2f €", totalPrice);
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    statusInfoLabel.setText(GeneralAPI.currentLog);
                    System.out.println(GeneralAPI.currentLog);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(null, "An error occurred, try again later. [429]");
            }
        };

        run.addActionListener(actionListener);
        constraints.gridx = 0;
        constraints.gridy = 11;
        panel.add(run, constraints);

        add(panel);

        pack();

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}