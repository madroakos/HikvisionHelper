package com.madroakos.hikvisionhelper.ffmpeg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ConcatVideo extends Thread {
    private final String[] filePath;
    private final String outputPath;

    public ConcatVideo(String[] filePath, String outputPath) {
        this.filePath = filePath;
        this.outputPath = outputPath;
    }

    public void run() {
        String fileName = "temp.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String s : filePath) {
                writer.write("file '" + s + "'");
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error occurred while creating the file: " + e.getMessage());
        }

        String command = String.format("\"ffmpeg.exe\" -y -f concat -safe 0 -i \"%s\" -c copy \"%s\"", fileName, outputPath);
        System.out.println(command);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO().start();
        } catch (IOException e) {
            System.out.println("""
                    Nem találja a program az "ffmpeg.exe" fájlt.
                    Kérlek helyezd el ide: /HikvisionHelper/bin/
                    ffmpeg: https://ffmpeg.org/download.html
                    """);
        }
    }
}