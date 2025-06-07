package ru.demyan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.demyan.domain.ProductLink;

@Repository
public interface ProductLinkRepository extends JpaRepository<ProductLink, Long> {
    ProductLink getProductLinkByItemId(Long itemId);
}
