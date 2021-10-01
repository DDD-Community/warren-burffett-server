package com.warrenbuffett.server.domain;

import javax.persistence.*;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@ToString
@Table(name="user")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(nullable = false)
    private String user_name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)  // 직접 case처리 or enumerated?
    @Column
    private UserOauthType userOauthType;


    @Builder
    public User(final String id, final String user_name, final String email, final String password, UserOauthType userOauthType) {
        this.id = id;
        this.user_name = user_name;
        this.password = password;
        this.email = email;
        this.userOauthType = userOauthType;
    }
}