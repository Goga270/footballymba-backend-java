package com.footballymba.api.features.player.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tg_id", unique = true)
    private Long tgId;

    @Column(name = "tg_username")
    private String tgUsername;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String position;

    @Builder.Default
    private Float rating = 0.0f;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PlayerTier tier = PlayerTier.S;
}