package com.madroakos.hikvisionhelper.ffmpeg;


import javax.swing.*;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FixVideoManager {
    private final ExecutorService executor;

    public FixVideoManager() {
        int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
    }

    public void fixVideo(File[] file, String outputPath) {
        CountDownLatch countDownLatch = new CountDownLatch(file.length);
        for (File f : file) {
            executor.execute(new FixVideo(f.getAbsolutePath(), outputPath + "\\FIXED_" + f.getName(), countDownLatch));
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
            JOptionPane.showMessageDialog(null, "Done!", "Information", JOptionPane.INFORMATION_MESSAGE);
        }


    }

    public void stopAllThreads() {
        executor.shutdownNow();
    }
}