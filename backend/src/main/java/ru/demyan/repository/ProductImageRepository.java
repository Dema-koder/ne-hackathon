package ru.demyan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.demyan.domain.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    ProductImage getProductImageByItemId(Long itemId);
    @Query("select pi.picLink from ProductImage pi where pi.id = :id")
    String getPicLinkById(Long id);
}
