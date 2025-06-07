package ru.demyan.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
@RequestMapping("/api")
public class DropboxController {
    @GetMapping("/downloadImage")
    public ResponseEntity<byte[]> getDropboxImage(@RequestParam String url) {
        try {
            String downloadUrl = convertToDownloadUrl(url);

            HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
            connection.setRequestMethod("GET");

            try (InputStream in = connection.getInputStream();
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                byte[] tmp = new byte[4096];
                int n;

                while ((n = in.read(tmp, 0, tmp.length)) != -1) {
                    buffer.write(tmp, 0, n);
                }

                byte[] imageBytes = buffer.toByteArray();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG);

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(imageBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private String convertToDownloadUrl(String url) {
        if (url.contains("dropbox.com")) {
            return url.replace("?dl=0", "?dl=1");
        }
        return url;
    }

}
