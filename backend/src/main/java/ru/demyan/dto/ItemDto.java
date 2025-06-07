package ru.demyan.dto;

import lombok.Data;

@Data
public class ItemDto {
    private Long id;
    private ImageDto image;
    private ProductLinkDto link;
}