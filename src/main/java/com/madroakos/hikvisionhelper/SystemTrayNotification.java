package com.madroakos.hikvisionhelper;

import java.awt.*;

public class SystemTrayNotification {
    public SystemTrayNotification(String title, String message) {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Notification");
            trayIcon.setImageAutoSize(true);
            try {
                tray.add(trayIcon);
                trayIcon.displayMessage(title,  message, TrayIcon.MessageType.INFO);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
        } else {
            System.err.println("SystemTray is not supported.");
        }
    }
}
