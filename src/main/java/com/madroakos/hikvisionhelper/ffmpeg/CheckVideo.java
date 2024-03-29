package com.madroakos.hikvisionhelper.ffmpeg;

import com.madroakos.hikvisionhelper.mainPage.ApplicationController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CheckVideo {
    private final String command;

    public CheckVideo (String filePath) {
        this.command = String.format("\"%s\" -i \"%s\"", ApplicationController.ffprobeFilePath, filePath);
        System.out.println(command);
    }

    public boolean hasAudio() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.inheritIO().start();

            try (InputStream inputStream = process.getErrorStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                return reader.lines().noneMatch(line -> line.contains("Audio"));
            }
        } catch (IOException e) {
            System.err.println("Error checking video: " + e.getMessage());
            return false;
        }
    }
}
