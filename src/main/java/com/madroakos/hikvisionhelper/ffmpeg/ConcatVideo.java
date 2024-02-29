package com.madroakos.hikvisionhelper.ffmpeg;

import com.madroakos.hikvisionhelper.SystemTrayNotification;
import com.madroakos.hikvisionhelper.mainPage.ApplicationController;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConcatVideo extends Thread {
    private final File[] filePath;
    private final String outputPath;
    private final ProgressBar progressBar;

    public ConcatVideo(File[] filePath, String outputPath, ProgressBar progressBar) {
        this.filePath = filePath;
        this.outputPath = outputPath;
        this.progressBar = progressBar;
    }

    public void run() {
        boolean hasAudio = false;
        File tempFile = new File("temp.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (File s : filePath) {
                writer.write("file '" + s.getAbsolutePath() + "'");
                writer.newLine();

                if (!hasAudio) {
                    CheckVideo checkVideo = new CheckVideo(s.getAbsolutePath());
                    if (checkVideo.hasAudio()) {
                        hasAudio = true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error occurred while creating the file: " + e.getMessage());
        }

        ProcessBuilder processBuilder = getCommand(tempFile.getAbsolutePath(), hasAudio);
        System.out.println(processBuilder.command());
        updateProgressBar(0.5);
        try {
            Process process = processBuilder.inheritIO().start();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            tempFile.deleteOnExit();
            new SystemTrayNotification("Done", "Merge finished!");
            updateProgressBar(1);
        }
    }

    private ProcessBuilder getCommand(String fileName, boolean hasAudio) {
        String commandForNoAudio = String.format("\"%s\" -y -f concat -safe 0 -i \"%s\" -c copy \"%s\"", ApplicationController.ffmpegFilePath, fileName, outputPath);
        String commandForAudio = String.format("\"%s\" -y -f concat -safe 0 -i \"%s\" -c:v copy -c:a aac \"%s\"", ApplicationController.ffmpegFilePath, fileName, outputPath);
        ProcessBuilder processBuilder;

        if (hasAudio) {
            processBuilder = new ProcessBuilder(commandForAudio);
        } else {
            processBuilder = new ProcessBuilder(commandForNoAudio);
        }
        return processBuilder;
    }

    private void updateProgressBar(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }
}