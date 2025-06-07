package ru.demyan.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "item_sex")
@IdClass(ItemSex.ItemSexId.class)
public class ItemSex {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Id
    @Column(nullable = false, length = 20)
    private String sex;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemSexId implements Serializable {
        private Long item;
        private String sex;
    }
}