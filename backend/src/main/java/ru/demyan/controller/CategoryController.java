package ru.demyan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.demyan.domain.Category;
import ru.demyan.domain.Item;
import ru.demyan.dto.CategoryDto;
import ru.demyan.dto.ImageDto;
import ru.demyan.dto.ItemDto;
import ru.demyan.dto.ProductLinkDto;
import ru.demyan.repository.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping(path = "/categories", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<List<CategoryDto>> getProducts(@RequestParam String sex) {
        List<Long> ids = itemSexRepository.getIdsBySex(sex);
        var items = itemRepository.findItemByIds(ids);

        Map<Long, Category> categoriesMap = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        Map<Category, List<ItemDto>> categoryItemsMap = new HashMap<>();

        for (Item item : items) {
            Category category = categoriesMap.get(item.getCategory().getId());
            if (category == null) continue;

            ItemDto itemDto = new ItemDto();
            itemDto.setId(item.getId());

            var image = productImageRepository.getProductImageByItemId(item.getId());
            ImageDto imageDto = new ImageDto();
            imageDto.setId(image.getId());
            imageDto.setPicLink(image.getPicLink());
            itemDto.setImage(imageDto);

            var link = productLinkRepository.getProductLinkByItemId(item.getId());
            ProductLinkDto linkDto = new ProductLinkDto();
            linkDto.setId(link.getId());
            linkDto.setProductLink(link.getProductLink());
            itemDto.setLink(linkDto);

            categoryItemsMap.computeIfAbsent(category, k -> new ArrayList<>()).add(itemDto);
        }

        List<CategoryDto> result = categoryItemsMap.entrySet().stream()
                .map(entry -> {
                    CategoryDto dto = new CategoryDto();
                    dto.setName(entry.getKey().getName());
                    dto.setItems(entry.getValue());
                    return dto;
                })
                .sorted(Comparator.comparing(
                        dto -> categoriesMap.values().stream()
                                .filter(c -> c.getName().equals(dto.getName()))
                                .findFirst()
                                .map(Category::getOrder)
                                .orElse(Long.MAX_VALUE)
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}