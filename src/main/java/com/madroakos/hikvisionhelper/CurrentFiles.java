package com.madroakos.hikvisionhelper;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CurrentFiles {
    private final File fileName;
    private String startDate;
    private String endDate;
    private final String FILENAME_PATTERN = "\\d{14}";
    private final String FILENAME_PATTERN_WITH_EXTENSION = "\\d{14}\\.mp4";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CurrentFiles (File filename) {
        this.fileName = filename;
        initialize();
    }

    private void initialize() {
        int countedTimes = checkFileName(fileName.getName());
        if (countedTimes == 1) {
            setTimesWithoutEndTime();
        } else if (countedTimes == 2) {
            setTimes();
        }
    }

    private void setTimes() {
        String timeStamp;
        int counter = 1;
        for (String s : fileName.getName().split("_")) {
            if (s.matches(FILENAME_PATTERN)) {
                if (counter == 1) {
                    timeStamp = s;
                    startDate = String.format("%s-%s-%s %s:%s:%s", timeStamp.substring(0,4), timeStamp.substring(4,6), timeStamp.substring(6,8), timeStamp.substring(8,10), timeStamp.substring(10,12), timeStamp.substring(12,14));
                    counter++;
                } else if (counter == 2) {
                    timeStamp = s;
                    endDate = String.format("%s-%s-%s %s:%s:%s", timeStamp.substring(0,4), timeStamp.substring(4,6), timeStamp.substring(6,8), timeStamp.substring(8,10), timeStamp.substring(10,12), timeStamp.substring(12,14));
                    break;
                }
            }
        }
    }

    private int checkFileName(String fileName) {
        int counter = 0;
        for (String s : fileName.split("_")) {
            if (s.matches(FILENAME_PATTERN) || s.matches(FILENAME_PATTERN_WITH_EXTENSION)) {
                counter++;
            }
        }
        return counter;
    }

    private void setTimesWithoutEndTime() {
        String timeStamp = fileName.getName().split("_")[1];
        startDate = String.format("%s-%s-%s %s:%s:%s", timeStamp.substring(0,4), timeStamp.substring(4,6), timeStamp.substring(6,8), timeStamp.substring(8,10), timeStamp.substring(10,12), timeStamp.substring(12,14));
        String commandForNoEndDate = String.format("\"%s\" -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 \"%s\"", ApplicationController.ffprobeFilePath, fileName.getAbsolutePath());
        System.out.println(commandForNoEndDate);
        ProcessBuilder processBuilder = new ProcessBuilder(commandForNoEndDate);
        try {
            Process process = processBuilder.start();

            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    double lengthInSeconds = Double.parseDouble(line);
                    calculateEndTime(startDate, lengthInSeconds);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Error parsing duration: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void calculateEndTime (String startDate, double lengthInSeconds) {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTemp = LocalDateTime.parse(startDate, formatter);
        startDateTemp = startDateTemp.plusSeconds((long) lengthInSeconds);
        endDate = startDateTemp.format(formatter);
    }

    public String getFileName() {
        return fileName.getAbsolutePath();
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public File getFile() {
        return fileName;
    }
    public LocalDateTime getStartDateInDate() {
        return LocalDateTime.parse(startDate, formatter);
    }
    public LocalDateTime getEndDateInDate() {
        return LocalDateTime.parse(endDate, formatter);
    }
}
