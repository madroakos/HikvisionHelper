package com.madroakos.hikvisionhelper.mainPage;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class TableViewContextMenuController {
    public static ContextMenu createContextMenu(File selectedItem, ApplicationController controller) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(event -> openFile(selectedItem));

        MenuItem openContainingFolderItem = new MenuItem("Open Containing Folder");
        openContainingFolderItem.setOnAction(event -> openContainingFolder(selectedItem));

        MenuItem deleteSelectedItem = new MenuItem("Delete");
        deleteSelectedItem.setOnAction(event -> controller.deleteSelectedItem());

        contextMenu.getItems().addAll(openItem, openContainingFolderItem, deleteSelectedItem);

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
