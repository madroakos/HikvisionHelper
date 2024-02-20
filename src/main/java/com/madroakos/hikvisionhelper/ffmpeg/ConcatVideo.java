package com.madroakos.hikvisionhelper.ffmpeg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConcatVideo extends Thread {
    private final File[] filePath;
    private final String outputPath;

    public ConcatVideo(File[] filePath, String outputPath) {
        this.filePath = filePath;
        this.outputPath = outputPath;
    }

    public void run() {
        String fileName = "temp.txt";
        boolean hasAudio = false;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
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


        ProcessBuilder processBuilder = getCommand(fileName, hasAudio);

        try {
            Process process = processBuilder.inheritIO().start();
            process.waitFor();
        } catch (IOException e) {
            System.out.println("""
                    Nem találja a program az "ffmpeg.exe" fájlt.
                    Kérlek helyezd el ide: /HikvisionHelper/bin/
                    ffmpeg: https://ffmpeg.org/download.html
                    """);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ProcessBuilder getCommand(String fileName, boolean hasAudio) {
        String commandForNoAudio = String.format("\"ffmpeg.exe\" -y -f concat -safe 0 -i \"%s\" -c copy \"%s\"", fileName, outputPath);
        String commandForAudio = String.format("\"ffmpeg.exe\" -y -f concat -safe 0 -i \"%s\" -c:v copy -c:a aac \"%s\"", fileName, outputPath);
        ProcessBuilder processBuilder;

        if (hasAudio) {
            processBuilder = new ProcessBuilder(commandForAudio);
        } else {
            processBuilder = new ProcessBuilder(commandForNoAudio);
        }
        return processBuilder;
    }
}