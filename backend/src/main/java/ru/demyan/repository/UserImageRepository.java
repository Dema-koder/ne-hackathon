package ru.demyan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.demyan.domain.UserImage;

@Repository
public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    @Query("select ui.imageData from UserImage ui where ui.id = :id")
    byte[] getImageDataById(Long id);
}
