package pt.apolomachado.sip;

import pt.apolomachado.sip.panels.StartPanel;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StartPanel().setVisible(true));
    }
}