package ru.demyan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.demyan.domain.Category;
import ru.demyan.domain.Item;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
