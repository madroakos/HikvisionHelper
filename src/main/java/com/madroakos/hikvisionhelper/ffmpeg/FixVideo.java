package com.madroakos.hikvisionhelper.ffmpeg;

import com.madroakos.hikvisionhelper.mainPage.ApplicationController;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class FixVideo extends Thread {
    private final String filePath;
    private final String outputPath;
    private final CountDownLatch countDownLatch;

    public FixVideo(String filePath, String outputPath, CountDownLatch countDownLatch) {
        this.filePath = filePath;
        this.outputPath = outputPath;
        this.countDownLatch = countDownLatch;
    }


    public void run() {
        CheckVideo checkVideo = new CheckVideo(filePath);
        String commandForNoAudio = String.format("\"%s\" -y -err_detect ignore_err -i \"%s\" -c copy \"%s\"", ApplicationController.ffmpegFilePath, filePath, outputPath);
        String commandForAudio = String.format("\"%s\" -y -err_detect ignore_err -i \"%s\" -c:v copy -c:a aac \"%s\"",ApplicationController.ffmpegFilePath, filePath, outputPath);
        ProcessBuilder processBuilder;

        try {
            if (checkVideo.hasAudio()) {
                processBuilder = new ProcessBuilder(commandForAudio);
            } else {
                processBuilder = new ProcessBuilder(commandForNoAudio);
            }
                System.out.println(processBuilder.command());
                Process process = processBuilder.inheritIO().start();
                int exitCode = process.waitFor();
                System.out.println("Process exited with code: " + exitCode);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            countDownLatch.countDown();
        }
    }
}