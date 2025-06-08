package ru.demyan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.demyan.domain.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    ProductImage getProductImageByItemId(Long itemId);

    @Query("select pi.picLink from ProductImage pi where pi.id = :id")
    String getPicLinkById(Long id);

    @Query("select pi.item from ProductImage pi where pi.id = :id")
    Long getItemIdById(Long id);

    @Query("SELECT i.category.id FROM ProductImage pi JOIN pi.item i WHERE pi.id = :imageId")
    Long findCategoryIdByImageId(@Param("imageId") Long imageId);
}
