package ru.demyan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.demyan.domain.UserImage;
import ru.demyan.repository.UserImageRepository;

@RestController
@RequestMapping("/api")
public class UserImageController {
    @Autowired
    private UserImageRepository userImageRepository;

    @PostMapping("/upload")
    public ResponseEntity<Long> uploadImage(@RequestBody byte[] imageBytes) {
        UserImage userImage = new UserImage();
        userImage.setImageData(imageBytes);
        userImage = userImageRepository.save(userImage);

        return new ResponseEntity<>(userImage.getId(), HttpStatus.CREATED);
    }

}
