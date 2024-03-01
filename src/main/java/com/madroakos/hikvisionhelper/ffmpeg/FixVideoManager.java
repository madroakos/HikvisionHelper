package com.madroakos.hikvisionhelper.ffmpeg;


import com.madroakos.hikvisionhelper.SystemTrayNotification;
import javafx.scene.control.ProgressBar;
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

    public void fixVideo(File[] file, String outputPath, ProgressBar progressBar) {
        int max = file.length;
        new Thread(() -> {
            CountDownLatch countDownLatch = new CountDownLatch(max);
            for (File f : file) {
                executor.execute(new FixVideo(f.getAbsolutePath(), outputPath + "\\FIXED_" + f.getName(), countDownLatch, max, progressBar));
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                executor.shutdown();
                SystemTrayNotification.getInstance().showMessage("Done", "Fix is completed");
            }
        }).start();
    }

    public void stopAllThreads() {
        executor.shutdownNow();
    }
}