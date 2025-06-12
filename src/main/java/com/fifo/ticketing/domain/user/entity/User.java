package com.fifo.ticketing.domain.user.entity;

import com.fifo.ticketing.global.entity.BaseDateEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private String username;

    private String provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean isBlocked;

    @Builder
    public User(Long id, String email, String password, String username, String provider) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.provider = provider;
    }

    public static User fromForm(String email, String password, String username){
        return new User(email, password, username, null);
    }

    public static User fromOAuth(String email, String username, String provider){
        return new User(email, null, username, provider);
    }

    private User(String email, String password, String username, String provider) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.provider = provider;
    }

    public void updateBlockedState() {
        isBlocked = !isBlocked;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public Long getId() { return id; }
    public String getProvider() { return provider; }
    public boolean isBlocked() { return isBlocked; }
  
}
