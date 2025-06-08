package ru.demyan.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
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
import reactor.util.retry.Retry;
import ru.demyan.repository.CategoryRepository;
import ru.demyan.repository.ItemRepository;
import ru.demyan.repository.ProductImageRepository;
import ru.demyan.repository.UserImageRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

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

    @Autowired
    public FittingController(WebClient webClient) {
        this.webClient = webClient.mutate()
                .baseUrl(apiUrl)
                .build();
    }

    @GetMapping("/fitting")
    public Mono<byte[]> fitting(
            @RequestParam("user-image-id") Long userImageId,
            @RequestParam("product-image-id") Long productImageId) {

        try {
            // 1. Получаем и конвертируем изображение пользователя в JPEG
            byte[] userImageJpeg = convertToJpeg(userImageRepository.getImageDataById(userImageId));

            // 2. Получаем и конвертируем товарное изображение в JPEG
            String productLink = convertToDownloadUrl(productImageRepository.getPicLinkById(productImageId));
            byte[] productImageJpeg = downloadAndConvertToJpeg(productLink);

            // 3. Определяем категорию
            int category = determineCategory(productImageId);

            // 4. Подготавливаем запрос
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model_image", Base64.getEncoder().encodeToString(userImageJpeg));
            requestBody.put("cloth_image", Base64.getEncoder().encodeToString(productImageJpeg));
            requestBody.put("category", category);
            requestBody.put("seed", 4652);

            var respons = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), response ->
                            response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Remote error: " + body)))
                    .bodyToMono(String.class) // Получаем как строку (JSON)
                    .flatMap(json -> {
                        try {
                            // Парсим JSON
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode root = mapper.readTree(json);

                            // Извлекаем base64 строку с картинкой
                            String base64Image = root.path("image").path("image_base64").asText();

                            // Удаляем возможный префикс (если есть)
                            if (base64Image.contains(",")) {
                                base64Image = base64Image.split(",")[1];
                            }

                            // Декодируем base64 в массив байт
                            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                            return Mono.just(imageBytes);
                        } catch (Exception e) {
                            return Mono.error(new RuntimeException("Failed to parse image from JSON", e));
                        }
                    });

            return respons;

        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to process images", e));
        }
    }

    private byte[] convertToJpeg(byte[] imageData) throws IOException {
        BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(imageData));
        if (image == null) {
            throw new IOException("Invalid image data");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }

    private byte[] downloadAndConvertToJpeg(String imageUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
        connection.setRequestMethod("GET");

        try (InputStream in = connection.getInputStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] tmp = new byte[4096];
            int n;

            while ((n = in.read(tmp, 0, tmp.length)) != -1) {
                buffer.write(tmp, 0, n);
            }

            return buffer.toByteArray();
        }
    }

    private int determineCategory(Long productImageId) {
        Long categoryId = productImageRepository.findCategoryIdByImageId(productImageId);
        if (categoryId == 1) return 2;
        if (categoryId == 2 || categoryId == 5 || categoryId == 11) return 0;
        return 1;
    }

    private String convertToDownloadUrl(String url) {
        if (url.contains("dropbox.com")) {
            return url.replace("?dl=0", "?dl=1");
        }
        return url;
    }
}
