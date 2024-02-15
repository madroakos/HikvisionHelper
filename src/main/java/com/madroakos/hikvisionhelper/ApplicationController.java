package com.madroakos.hikvisionhelper;

import com.madroakos.hikvisionhelper.ffmpeg.FixVideoManager;
import com.madroakos.hikvisionhelper.ffmpeg.ConcatVideo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ApplicationController {

    @FXML
    private ListView<String> myList;

    @FXML
    private Button addButton;
    @FXML
    private Button mergeButton;
    @FXML
    private Label itemLabel;

    private FixVideoManager manager;
    private final Queue<Long> mouseClickTimes = new LinkedList<>();

    public ApplicationController() {
    }

    @FXML
    protected void onAddButtonClick() {
        Stage stage = (Stage) addButton.getScene().getWindow();
        List<String> listOfFiles = showFileChooserDialog(stage);

        if (listOfFiles != null) {
            for (int i = 0; i < listOfFiles.size(); i++) {
                if (myList.getItems().contains(listOfFiles.get(i))) {
                    listOfFiles.remove(i);
                }
            }

            ObservableList<String> fileNames = FXCollections.observableArrayList();
            fileNames.addAll(listOfFiles);
            myList.getItems().addAll(fileNames);
            itemLabel.setText(myList.getItems().size() + " items");


        }
    }

    @FXML
    protected void onDeleteKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            if (myList.getSelectionModel().getSelectedIndex() > -1) {
                myList.getItems().remove(myList.getSelectionModel().getSelectedIndex());
                itemLabel.setText(myList.getItems().size() + " items");
            }
        }
    }

    @FXML
    protected void onFixButtonClicked() {
        if (!myList.getItems().isEmpty()) {
            manager = new FixVideoManager();
            String outputPath = showFolderChooserDialog((Stage)addButton.getScene().getWindow());
            File[] files = myList.getItems().stream().map(File::new).toArray(File[]::new);
            manager.fixVideo(files, outputPath);
        }
    }

    @FXML
    protected void onClearButtonClicked() {
        myList.getItems().clear();
        itemLabel.setText("0 items");
    }

    @FXML
    protected void onFixAndMergeButtonClicked() {
        if(myList.getItems().size() > 1) {
            manager = new FixVideoManager();
            Stage stage = (Stage)mergeButton.getScene().getWindow();
            String outputPath = showSingleFileChooserDialog(stage);
            String[] filePath = new String[myList.getItems().size()];
            for (int i = 0; i < myList.getItems().size(); i++) {
                filePath[i] = myList.getItems().get(i);
            }

            ConcatVideo concatVideo = new ConcatVideo(filePath, outputPath);
            concatVideo.start();

            File[] file = new File[1];
            file[0] = new File(outputPath);

            manager.fixVideo(file, outputPath);
        }
    }

    @FXML
    protected void onMergeButtonClicked() {
        if(myList.getItems().size() > 1) {
            manager = new FixVideoManager();
            Stage stage = (Stage)mergeButton.getScene().getWindow();
            String outputPath = showSingleFileChooserDialog(stage);
            String[] filePath = new String[myList.getItems().size()];
            for (int i = 0; i < myList.getItems().size(); i++) {
                filePath[i] = myList.getItems().get(i);
            }

            ConcatVideo concatVideo = new ConcatVideo(filePath, outputPath);
            concatVideo.start();
        }
    }

    @FXML
    protected void onAbortButtonClicked() {
        manager.stopAllThreads();
        manager = new FixVideoManager();
    }

    private List<String> showFileChooserDialog(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles != null) {
            List<String> absolutePaths = new ArrayList<>();
            for (File file : selectedFiles) {
                if (isMp4File(file)) {
                    absolutePaths.add(file.getAbsolutePath());
                } else {
                    showWarningDialog();
                    return null;
                }
            }
            return absolutePaths;
        } else {
            return null;
        }
    }

    private String showSingleFileChooserDialog(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enter name of file to save to...");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showSaveDialog(stage);

        return selectedFile.getAbsolutePath();
    }

    private String showFolderChooserDialog(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to save to...");

        File selectedFolder = directoryChooser.showDialog(stage);

        if (selectedFolder != null) {
            return selectedFolder.getAbsolutePath();
        }
        return "";
    }

    private boolean isMp4File(File file) {
        return file.getName().toLowerCase().endsWith(".mp4");
    }

    private void showWarningDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Incompatible type");
        alert.setHeaderText(null);
        alert.setContentText("Incompatible file(s) provided");
        alert.showAndWait();
    }

    @FXML
    protected void onMyListMouseClicked(Event event) {
        if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED)) {
            if (myList.getSelectionModel().getSelectedIndex() > -1) {
                long currentTime = System.currentTimeMillis();
                if (mouseClickTimes.isEmpty()) {
                    mouseClickTimes.add(currentTime);
                } else {
                    if (currentTime - mouseClickTimes.remove() < 500) {
                        File currentFile = new File(myList.getSelectionModel().getSelectedItem());
                        if (currentFile.isFile() && currentFile.canRead()) {
                            try {
                                Desktop.getDesktop().open(currentFile);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        mouseClickTimes.add(currentTime);
                    }
                }
            }
        }
    }
}