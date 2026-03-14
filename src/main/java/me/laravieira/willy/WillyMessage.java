package me.laravieira.willy;

import lombok.Getter;
import lombok.Setter;
import me.laravieira.willy.utils.PassedInterval;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
public class WillyMessage {
    protected final UUID id = UUID.randomUUID();
    @Setter
    protected Context context;
    @Setter
    protected String from;
    @Setter
    protected String to;
    protected Object content;
    @Setter
    protected String text = "";
    protected PassedInterval expire = new PassedInterval(PassedInterval.DISABLE);
    protected List<File> images = new ArrayList<>();
    protected List<File> audios = new ArrayList<>();
    protected List<File> documents = new ArrayList<>();
    protected List<String> urls = new ArrayList<>();
    protected long created = new Date().getTime();

    public WillyMessage(Object content) {
        this.content = content;
        this.expire.start();
    }

    /** Set the message to destroy itself after expire time */
    public void setExpire(long expire) {
        this.expire = new PassedInterval(expire);
        this.expire.start();
    }

    /** Add attachment file, it will be categorized by its mime type */
    public void addFile(File file) {
        try {
            String mineType = Files.probeContentType(file.toPath());
            if (mineType == null) documents.add(file);
            assert mineType != null;
            if (mineType.startsWith("image")) images.add(file);
            else if (mineType.startsWith("audio")) audios.add(file);
            else documents.add(file);
        } catch (IOException e) {
            Willy.getLogger().warning(e.getMessage());
        }
    }

    /** Add attachment file by url, it will be downloaded and categorized by its mime type */
    public void addFile(String url) {
        HttpURLConnection httpConn = null;
        try {
            httpConn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            httpConn.setRequestProperty("User-Agent", Willy.getName()+" "+Willy.getFullVersion());
            httpConn.setConnectTimeout(5000); // 5 seconds
            httpConn.setReadTimeout(7000); // 7 seconds
            int responseCode = httpConn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK)
                throw new IOException("No file to download. Server replied HTTP code: " + responseCode);

            File file = File.createTempFile("willy-", null);

            try (InputStream inputStream = new BufferedInputStream(httpConn.getInputStream());
                FileOutputStream outputStream = new FileOutputStream(file)) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1)
                    outputStream.write(buffer, 0, bytesRead);
            }
            this.addFile(file);
        } catch (IOException e) {
            Willy.getLogger().warning(e.getMessage());
        } finally {
            if (httpConn != null) httpConn.disconnect();
        }
    }

    /** Add URL, not as an attachment, just URL string. */
    public void addUrl(String url) {
        urls.add(url);
    }

    /** Destroy the message content, including files attached */
    public void destroy() {
        content = null;
        for (File file : images) file.delete();
        for (File file : audios) file.delete();
        for (File file : documents) file.delete();
        images.clear();
        audios.clear();
        documents.clear();
        urls.clear();
    }
}
