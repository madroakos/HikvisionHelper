package com.madroakos.hikvisionhelper;

import java.awt.*;

public class SystemTrayNotification {
    private static SystemTrayNotification instance = new SystemTrayNotification();
    private SystemTray tray;
    private TrayIcon trayIcon;
    public SystemTrayNotification() {
        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("src/main/resources/icons/logo.png");
            Dimension trayIconSize = tray.getTrayIconSize();
            image = image.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);
            trayIcon = new TrayIcon(image, "HikvisionHelper");
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
        } else {
            System.err.println("SystemTray is not supported.");
        }
    }

    public void shutDown() {
        tray.remove(trayIcon);
    }

    public static SystemTrayNotification getInstance() {
        if (instance == null) {
            instance = new SystemTrayNotification();
        }
        return instance;
    }

    public void showMessage(String caption, String text) {
        trayIcon.displayMessage(caption, text, TrayIcon.MessageType.INFO);
    }
}
