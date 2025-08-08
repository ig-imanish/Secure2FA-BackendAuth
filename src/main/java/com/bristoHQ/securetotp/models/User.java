package com.bristoHQ.securetotp.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bristoHQ.securetotp.models.premium.RedeemCode;
import com.bristoHQ.securetotp.models.role.Role;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Document(collection = "my_users")
@Getter
@Setter
@AllArgsConstructor
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements UserDetails {

    @Id
    String id;
    String fullName;
    String username;
    String email;
    String password;

    List<Role> roles;

    String userAvatar;
    String userAvatarpublicId;

    String userBanner;
    String userBannerpublicId;

    String recoveryPhone;
    String recoveryEmail;

    Date birthDate;
    String gender;

    String provider;
    boolean isPremium;
    RedeemCode redeemCode;

    Date accountCreatedAt;
    Date lastActiveAt;
    Date profileUpdatedAt;

    boolean verified = false;
    String otp;
    LocalDateTime otpGeneratedTime;

    public User(String username, String email, String password, List<Role> roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.accountCreatedAt = new Date();
        this.lastActiveAt = new Date();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        this.roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getRoleName())));
        return authorities;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void updateLastActive() {
        this.lastActiveAt = new Date();
    }
}