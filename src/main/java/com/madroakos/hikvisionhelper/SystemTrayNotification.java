package com.madroakos.hikvisionhelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SystemTrayNotification {
    private static SystemTrayNotification instance = new SystemTrayNotification();
    private SystemTray tray;
    private TrayIcon trayIcon;
    private PopupMenu popupMenu;
    public SystemTrayNotification() {
        if (SystemTray.isSupported()) {
            popupMenu = new PopupMenu();
            MenuItem exitItem = new MenuItem("Exit");
            popupMenu.add(exitItem);


            tray = SystemTray.getSystemTray();
            InputStream inputStream = getClass().getResourceAsStream("/icons/logo.png");
            Image image;
            try {
                image = ImageIO.read(Objects.requireNonNull(inputStream));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Dimension trayIconSize = tray.getTrayIconSize();
            image = image.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);
            trayIcon = new TrayIcon(image, "HikvisionHelper", popupMenu);
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
    public PopupMenu getPopupMenu() {
        return popupMenu;
    }
}
