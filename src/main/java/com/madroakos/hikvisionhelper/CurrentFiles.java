package com.madroakos.hikvisionhelper;

import com.madroakos.hikvisionhelper.mainPage.ApplicationController;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CurrentFiles {
    private final File file;
    private String startDate;
    private String endDate;
    private static final String FILENAME_PATTERN = "\\d{14}";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CurrentFiles(File file) {
        this.file = file;
        initialize();
    }

    private void initialize() {
        int count = checkFileName(file.getName());
        if (count == 1) {
            setTimesWithoutEndTime();
        } else if (count == 2) {
            setTimes();
        }
    }

    private void setTimes() {
        String timeStamp;
        int counter = 1;
        for (String part : file.getName().split("_")) {
            if (part.matches(FILENAME_PATTERN)) {
                timeStamp = part;
                if (counter == 1) {
                    startDate = formatTimestamp(timeStamp);
                } else if (counter == 2) {
                    endDate = formatTimestamp(timeStamp);
                    break;
                }
                counter++;
            }
        }
    }

    public int checkFileName(String fileName) {
        int counter = 0;
        for (String part : fileName.split("_")) {
            if (part.matches(FILENAME_PATTERN) || part.matches(FILENAME_PATTERN + ".mp4")) {
                counter++;
            }
        }
        return counter;
    }

    private String calculateEndTime(String startDate, double lengthInSeconds) {
        try {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate, FORMATTER);
            LocalDateTime endDateTime = startDateTime.plusSeconds((long) lengthInSeconds);
            return endDateTime.format(FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Error parsing start date: " + e.getMessage(), e);
        }
    }

    private void setTimesWithoutEndTime() {
        String timeStamp = getTimestampFromFileName();
        startDate = formatTimestamp(timeStamp);
        String commandForNoEndDate = String.format("\"%s\" -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"", ApplicationController.ffprobeFilePath, file.getAbsolutePath());
        System.out.println(commandForNoEndDate);
        try {
            double lengthInSeconds = getVideoLength(commandForNoEndDate);
            endDate = calculateEndTime(startDate, lengthInSeconds);
        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Error setting times without end time: " + e.getMessage(), e);
        }
    }

    private String getTimestampFromFileName() {
        String[] parts = file.getName().split("_");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid file name format: " + file.getName());
        }
        return parts[1];
    }

    private String formatTimestamp(String timeStamp) {
        return String.format("%s-%s-%s %s:%s:%s", timeStamp.substring(0, 4), timeStamp.substring(4, 6), timeStamp.substring(6, 8), timeStamp.substring(8, 10), timeStamp.substring(10, 12), timeStamp.substring(12, 14));
    }

    private double getVideoLength(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();
        try (InputStream inputStream = process.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            if ((line = reader.readLine()) != null) {
                return Double.parseDouble(line);
            }
        }
        throw new IOException("No duration information found.");
    }

    public String getFileName() {
        return file.getAbsolutePath();
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public File getFile() {
        return file;
    }

    public LocalDateTime getStartDateInDate() {
        return LocalDateTime.parse(startDate, FORMATTER);
    }

    public LocalDateTime getEndDateInDate() {
        return LocalDateTime.parse(endDate, FORMATTER);
    }
}
