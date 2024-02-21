package com.madroakos.hikvisionhelper.mainPage;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TableViewContextMenuController {
    public static ContextMenu createContextMenu(File selectedItem) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(event -> openFile(selectedItem));

        MenuItem openContainingFolderItem = new MenuItem("Open Containing Folder");
        openContainingFolderItem.setOnAction(event -> openContainingFolder(selectedItem));

        contextMenu.getItems().addAll(openItem, openContainingFolderItem);

        return contextMenu;
    }

    private static void openFile(File selectedItem) {
        if (selectedItem.isFile() && selectedItem.canRead()) {
            try {
                Desktop.getDesktop().open(selectedItem);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void openContainingFolder(File selectedItem) {
        File folder = selectedItem.getParentFile();
        if (folder != null) {
            try {
                Desktop.getDesktop().open(folder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
