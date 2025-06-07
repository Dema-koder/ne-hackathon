package ru.demyan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.demyan.domain.ItemSex;

import java.util.List;

@Repository
public interface ItemSexRepository extends JpaRepository<ItemSex, ItemSex.ItemSexId> {

    @Query("SELECT is.item.id FROM ItemSex is WHERE is.sex = 'унисекс' OR is.sex = :sex")
    List<Long> getIdsBySex(String sex);
}
