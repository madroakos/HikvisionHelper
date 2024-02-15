package com.madroakos.hikvisionhelper.ffmpeg;

import java.io.IOException;
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
        String command = String.format("\"ffmpeg.exe\" -y -err_detect ignore_err -i \"%s\" -c copy \"%s\"", filePath, outputPath);
        System.out.println(command);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
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
            System.out.println("Process interrupted!");
            System.out.println(Arrays.toString(e.getStackTrace()));
        } finally {
            countDownLatch.countDown();
        }
    }
}