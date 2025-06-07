package ru.demyan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.demyan.repository.ProductImageRepository;
import ru.demyan.repository.UserImageRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@RestController
@RequestMapping("/api")
public class FittingController {

    @Autowired
    private UserImageRepository userImageRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @GetMapping("/fitting")
    public ResponseEntity<byte[]> fitting(@RequestParam("user-image-id") Long userImageId,
                                          @RequestParam("product-image-id") Long productImageId) throws IOException {
        byte[] userImage = userImageRepository.getImageDataById(userImageId);
        String productLink = productImageRepository.getPicLinkById(productImageId);
        String downloadUrl = convertToDownloadUrl(productLink);

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
            return ResponseEntity.ok()
                    .body(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String convertToDownloadUrl(String url) {
        if (url.contains("dropbox.com")) {
            return url.replace("?dl=0", "?dl=1");
        }
        return url;
    }
}
