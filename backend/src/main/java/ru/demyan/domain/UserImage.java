package ru.demyan.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "user_images")
@Entity
@Data
@NoArgsConstructor
public class UserImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private byte[] imageData;
}
