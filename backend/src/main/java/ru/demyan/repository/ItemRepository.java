package ru.demyan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.demyan.domain.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("select i from Item i where i.id in :ids")
    List<Item> findItemByIds(@Param("ids") List<Long> ids);

    @Query("select i.category from Item i where i.id = :id")
    Long findCategoryIdById(Long id);
}
