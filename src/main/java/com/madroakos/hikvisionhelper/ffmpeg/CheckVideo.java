package com.madroakos.hikvisionhelper.ffmpeg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CheckVideo {
    private final String command;

    public CheckVideo (String filePath) {
        this.command = "\"ffmpeg.exe\" -i \"" + filePath + "\"";
        System.out.println(command);
    }

    public boolean hasAudio () throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.inheritIO().start();
        InputStream inputStream = process.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        return reader.lines().noneMatch(line -> line.contains("Audio"));
    }
}
