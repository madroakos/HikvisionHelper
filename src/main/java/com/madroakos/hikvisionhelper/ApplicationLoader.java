package com.madroakos.hikvisionhelper;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Objects;

public class ApplicationLoader extends javafx.application.Application {

    public static void main(String[] args) {
        launch();
    }

    private static void run() {
        System.exit(0);
    }

    private static void actionPerformed(ActionEvent e) {
        Platform.runLater(ApplicationLoader::run);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainPage/mainpage-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("HikvisionHelper");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(ApplicationLoader.class.getResourceAsStream("/icons/logo.png"))));
        stage.show();
        SystemTrayNotification.getInstance().getPopupMenu().getItem(0).addActionListener(ApplicationLoader::actionPerformed);

        stage.setOnCloseRequest(event -> SystemTrayNotification.getInstance().shutDown());
    }
}