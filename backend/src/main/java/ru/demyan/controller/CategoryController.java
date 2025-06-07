package ru.demyan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.demyan.domain.Item;
import ru.demyan.dto.CategoryDto;
import ru.demyan.dto.ImageDto;
import ru.demyan.dto.ItemDto;
import ru.demyan.dto.ProductLinkDto;
import ru.demyan.repository.ItemRepository;
import ru.demyan.repository.ItemSexRepository;
import ru.demyan.repository.ProductImageRepository;
import ru.demyan.repository.ProductLinkRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private ItemSexRepository itemSexRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductLinkRepository productLinkRepository;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getProducts(@RequestParam String sex) {
        List<Long> ids = itemSexRepository.getIdsBySex(sex);
        var items = itemRepository.findItemByIds(ids);
        Map<String, CategoryDto> categoryMap = new HashMap<>();

        for (Item item : items) {
            String categoryName = item.getCategory().getName();
            CategoryDto categoryDto = categoryMap.getOrDefault(categoryName, new CategoryDto());
            categoryDto.setName(categoryName);

            ItemDto itemDto = new ItemDto();
            itemDto.setId(item.getId());

            var productImage = productImageRepository.getProductImageByItemId(item.getId());
            ImageDto imageDto = new ImageDto();
            imageDto.setId(productImage.getId());
            imageDto.setPicLink(productImage.getPicLink());
            itemDto.setImage(imageDto);

            var productLink = productLinkRepository.getProductLinkByItemId(item.getId());
            ProductLinkDto linkDto = new ProductLinkDto();
            linkDto.setId(productLink.getId());
            linkDto.setProductLink(productLink.getProductLink());
            itemDto.setLink(linkDto);

            categoryDto.getItems().add(itemDto);
            categoryMap.put(categoryName, categoryDto);
        }

        return ResponseEntity.ok(new ArrayList<>(categoryMap.values()));
    }
}