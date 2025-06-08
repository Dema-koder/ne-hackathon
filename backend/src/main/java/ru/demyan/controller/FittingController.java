package ru.demyan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.demyan.repository.CategoryRepository;
import ru.demyan.repository.ItemRepository;
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

    @Autowired
    private ItemRepository itemRepository;

    private final WebClient webClient;
    private final String apiUrl = "http://95.213.247.96:5000/generate";

    public FittingController() {
        this.webClient = WebClient.create(apiUrl);
    }

    @GetMapping("/fitting")
    public Mono<byte[]> fitting(@RequestParam("user-image-id") Long userImageId,
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
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            builder.part("cloth_image", new ByteArrayResource(imageBytes))
                    .filename("cloth.jpg")
                    .contentType(MediaType.IMAGE_JPEG);

            builder.part("model_image", new ByteArrayResource(userImage))
                    .filename("model.jpg")
                    .contentType(MediaType.IMAGE_JPEG);

            int category;
            Long xx = (itemRepository.findCategoryIfById(productImageRepository.getItemIdById(productImageId)));
            if (xx == 1)
                category = 2;
            else
            if (xx == 2 || xx == 5)
                category = 0;
            else
                category = 1;

            builder.part("category", category);

            return webClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .bodyToMono(byte[].class);
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
