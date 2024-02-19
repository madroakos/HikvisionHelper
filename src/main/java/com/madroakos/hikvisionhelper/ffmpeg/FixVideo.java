package com.madroakos.hikvisionhelper.ffmpeg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
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
        String commandForNoAudio = String.format("\"ffmpeg.exe\" -y -err_detect ignore_err -i \"%s\" -c copy \"%s\"", filePath, outputPath);
        String commandForAudio = String.format("\"ffmpeg.exe\" -y -err_detect ignore_err -i \"%s\" -c:v copy -c:a aac \"%s\"", filePath, outputPath);
        ProcessBuilder processBuilder;

        try {
            if (checkVideo.hasAudio()) {
                processBuilder = new ProcessBuilder(commandForAudio);
            } else {
                processBuilder = new ProcessBuilder(commandForNoAudio);
            }
                Process process = processBuilder.inheritIO().start();
                int exitCode = process.waitFor();
                System.out.println("Process exited with code: " + exitCode);
        } catch (IOException e) {
            System.out.println("""
                    "ffmpeg.exe" is not found!
                    Please place here: /HikvisionHelper/bin/
                    ffmpeg: https://ffmpeg.org/download.html
                    """);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            countDownLatch.countDown();
        }
    }
}