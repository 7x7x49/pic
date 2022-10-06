package ru.compot;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    private static final String IN_FILE = "input.txt";
    private static final Pattern DOWNLOAD_URL_PATTERN = Pattern.compile("data-image=\"(.+?)\"");

    public static void main(String[] args) {
        String link;
        try (BufferedReader br = new BufferedReader(new FileReader(IN_FILE))) {
            link = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        List<String> paths = new ArrayList<>();
        try {
            URL url = new URL(link);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String result = br.lines().collect(Collectors.joining("\n"));
            Matcher matcher = DOWNLOAD_URL_PATTERN.matcher(result);
            while (matcher.find() && paths.size() < 15) {
                paths.add(matcher.group(1));
            }
            System.out.printf("Найдено %d файлов\n", paths.size());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        File outputDir = new File("output");
        if (!outputDir.exists()) outputDir.mkdir();
        Thread[] threads = new Thread[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            Thread thread = new ThreadDownloader(paths.get(i));
            thread.setName("image-" + i + ".jpg");
            thread.start();
            threads[i] = thread;
        }
        while (true) {
            boolean exit = true;
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    exit = false;
                    break;
                }
            }
            if (exit) break;
        }
    }

    private static class ThreadDownloader extends Thread {

        private final String link;
        private long startTime;

        private ThreadDownloader(String link) {
            this.link = link;
        }

        @Override
        public synchronized void start() {
            super.start();
            System.out.println("Скачивание файла " + link);
            startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            try {
                URL url = new URL(link);
                ReadableByteChannel byteChannel = Channels.newChannel(url.openStream());
                FileOutputStream stream = new FileOutputStream("output\\" + getName());
                stream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
                stream.close();
                byteChannel.close();
                System.out.printf("Файл %s скачан за %.2f секунд\n", getName(), (System.currentTimeMillis() - startTime) / 1000f);
            } catch (Exception e) {
                System.out.println("Произошла ошибка при скачивании файла " + getName());
                e.printStackTrace();
            }

        }
    }

}