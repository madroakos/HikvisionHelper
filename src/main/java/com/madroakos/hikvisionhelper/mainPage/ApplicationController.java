package com.madroakos.hikvisionhelper.mainPage;

import com.madroakos.hikvisionhelper.CurrentFiles;
import com.madroakos.hikvisionhelper.ffmpeg.FixVideoManager;
import com.madroakos.hikvisionhelper.ffmpeg.ConcatVideo;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class ApplicationController implements Initializable {

    @FXML
    private TableView<CurrentFiles> myList;
    @FXML
    private TableColumn<CurrentFiles, String> nameColumn;
    @FXML
    private TableColumn<CurrentFiles, String> startTime;
    @FXML
    private TableColumn<CurrentFiles, String> endTime;



    @FXML
    private Button addButton;
    @FXML
    private Button mergeButton;
    @FXML
    private Label itemLabel;

    private FixVideoManager manager;
    private final Queue<Long> mouseClickTimes = new LinkedList<>();
    private final Queue<Integer> selectedRowOnClick = new LinkedList<>();
    private final ArrayList<CurrentFiles> currentFiles = new ArrayList<>();
    private static final Stage stage = new Stage();
    public static String ffmpegFilePath = "ffmpeg";
    public static String ffprobeFilePath = "ffprobe";
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFileName()));
        startTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStartDate()));
        endTime.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEndDate()));
        checkPreRequisites();
    }

    @FXML
    protected void onAddButtonClick() {
        List<String> listOfFiles = showFileChooserDialog();


        if (listOfFiles != null) {
            listOfFiles.removeIf(s -> myList.getItems().contains(s));

            for (String s : listOfFiles) {
                currentFiles.add(new CurrentFiles(new File(s)));
            }

            ObservableList<CurrentFiles> observableList = FXCollections.observableArrayList(currentFiles);
            myList.setItems(observableList);

            itemLabel.setText(myList.getItems().size() + " items");
        }
    }

    @FXML
    protected void onDeleteKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            deleteSelectedItem();
        }
    }

    public void deleteSelectedItem() {
        if (myList.getSelectionModel().getSelectedIndex() > -1) {
            currentFiles.remove(myList.getSelectionModel().getSelectedIndex());
            myList.getItems().remove(myList.getSelectionModel().getSelectedIndex());
            itemLabel.setText(myList.getItems().size() + " items");
        }
    }

    @FXML
    protected void onFixButtonClicked() {
        if (!myList.getItems().isEmpty()) {
            manager = new FixVideoManager();
            String outputPath = showFolderChooserDialog();
            if (!(outputPath == null)) {
                manager.fixVideo(getFilePaths() ,outputPath);
            }
        }
    }

    @FXML
    protected void onRightClickOnTableView() {
        if (myList.getSelectionModel().getSelectedIndex() > -1) {
            ContextMenu contextMenu = TableViewContextMenuController.createContextMenu(
                    currentFiles.get(myList.getSelectionModel().getSelectedIndex()).getFile(),
                    this
            );
            contextMenu.show(myList, MouseInfo.getPointerInfo().getLocation().getX(), MouseInfo.getPointerInfo().getLocation().getY());
        }
    }

    private File[] getFilePaths() {
        File[] file = new File[currentFiles.size()];
        for (int i = 0; i < currentFiles.size(); i++) {
            file[i] = currentFiles.get(i).getFile();
        }
        return file;
    }

    @FXML
    protected void onClearButtonClicked() {
        currentFiles.clear();
        myList.getItems().clear();
        itemLabel.setText("0 items");
    }

    @FXML
    protected void onMergeButtonClicked() {
        if(myList.getItems().size() > 1) {
            manager = new FixVideoManager();
            String outputPath;
            if (currentFiles.getFirst().checkFileName(currentFiles.getFirst().getFileName()) != 0) {
                outputPath = showSingleFileChooserDialog(getFileNameWithoutTimestamp(currentFiles.getFirst().getFile().getName()), getFilesMinDate(), getFilesMaxDate());
            } else {
                outputPath = showSingleFileChooserDialog(currentFiles.getFirst().getFile().getName());
            }

            if (outputPath != null) {
                ConcatVideo concatVideo = new ConcatVideo(getFilePaths(), outputPath);
                concatVideo.start();
            }
        }
    }

    private String getFileNameWithoutTimestamp(String filename) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : filename.split("_")) {
            if (!s.matches("\\d{14}")) {
                stringBuilder.append(s);
            } else {
                return stringBuilder.toString();
            }
        }
        return null;
    }

    private String getFilesMinDate() {
        LocalDateTime min = currentFiles.getFirst().getStartDateInDate();
        for (int i = 1; i < currentFiles.size(); i++) {
            if (currentFiles.get(i).getStartDateInDate().isBefore(min)) {
                min = currentFiles.get(i).getStartDateInDate();
            }
        }
        return min.format(formatter);
    }

    private String getFilesMaxDate() {
        LocalDateTime max = currentFiles.getFirst().getEndDateInDate();
        for (int i = 1; i < currentFiles.size(); i++) {
            if (currentFiles.get(i).getEndDateInDate().isAfter(max)) {
                max = currentFiles.get(i).getEndDateInDate();
            }
        }
        return max.format(formatter);
    }

    @FXML
    protected void onAbortButtonClicked() {
        manager.stopAllThreads();
        manager = new FixVideoManager();
    }

    private List<String> showFileChooserDialog() {
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

    private String showSingleFileChooserDialog(String filename, String startDate, String endDate) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enter name of file to save to...");
        fileChooser.setInitialFileName(String.format("%s_%s_%s", filename, startDate, endDate));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile;
        if ((selectedFile = fileChooser.showSaveDialog(stage)) != null) {
            return selectedFile.getAbsolutePath();
        } else {
            return null;
        }

    }


    private String showSingleFileChooserDialog(String filename) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enter name of file to save to...");
        fileChooser.setInitialFileName(filename);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile;
        if ((selectedFile = fileChooser.showSaveDialog(stage)) != null) {
            return selectedFile.getAbsolutePath();
        } else {
            return null;
        }

    }

    private String showFolderChooserDialog() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to save to...");

        File selectedFolder = directoryChooser.showDialog(stage);

        if (selectedFolder != null) {
            return selectedFolder.getAbsolutePath();
        }
        return null;
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
                    selectedRowOnClick.add(myList.getSelectionModel().getSelectedIndex());
                } else {
                    if (currentTime - mouseClickTimes.remove() < 500 && myList.getSelectionModel().getSelectedIndex() == selectedRowOnClick.remove()) {
                        File choosenFile = new File(String.valueOf(myList.getSelectionModel().getSelectedItem().getFileName()));
                        if (choosenFile.isFile() && choosenFile.canRead()) {
                            try {
                                Desktop.getDesktop().open(choosenFile);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        mouseClickTimes.add(currentTime);
                        if (!selectedRowOnClick.isEmpty()) {
                            selectedRowOnClick.remove();
                        }
                        selectedRowOnClick.add(myList.getSelectionModel().getSelectedIndex());
                    }
                }
            }
        }
    }

    private void checkPreRequisites() {
        if (!isFfmpegExecutableInPath()) {
            JOptionPane.showMessageDialog(null, "Okay!", "Please locate ffmpeg and ffprobe", JOptionPane.INFORMATION_MESSAGE);
            if (((ffmpegFilePath = locateMissingFiles("ffmpeg")).isEmpty()) ||
                    ((ffprobeFilePath = locateMissingFiles("ffprobe")).isEmpty())) {
                Platform.exit();
            }
        }
    }

    private String locateMissingFiles(String fileToBeFound) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(String.format("Please locate %s", fileToBeFound));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(fileToBeFound, fileToBeFound + ".exe"));

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        } else {
            Platform.exit();
            return "";
        }
    }

    private static boolean isFfmpegExecutableInPath() {
        String pathEnv = System.getenv("PATH");
        String[] pathDirs = pathEnv.split(File.pathSeparator);

        for (String pathDir : pathDirs) {
            File executableFile = new File(pathDir,"ffmpeg.exe");
            if (executableFile.exists() && executableFile.isFile()) {
                return true;
            }
        }
        return false;
    }
}