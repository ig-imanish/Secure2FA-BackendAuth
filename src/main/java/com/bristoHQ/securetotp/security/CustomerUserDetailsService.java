package com.bristoHQ.securetotp.security;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bristoHQ.securetotp.helper.EncryptionService;
import com.bristoHQ.securetotp.models.User;
import com.bristoHQ.securetotp.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {

    @Lazy
    private final UserRepository userRepository;

    @Autowired
    private EncryptionService encryptionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> users = userRepository.findByEmailOrUsername(encryptionService.encrypt(username),
                encryptionService.encrypt(username));
                System.out.println("Searching for user with encrypted username: " + encryptionService.encrypt(username));
        List<User> userList = users.stream().toList();

        if (userList.isEmpty()) {
            throw new UsernameNotFoundException("Invalid username or password.");
        } else if (userList.size() > 1) {
            throw new RuntimeException("Multiple users found for username: " + username);
        }

        User user = userList.get(0);
        System.out.println("User CustomerUserDetails: " + user);
        
        // Decrypt username, but use password as-is since it's a BCrypt hash
        String decryptedUsername = encryptionService.decrypt(user.getUsername());
        String password = user.getPassword(); // BCrypt hash, don't decrypt
        
        // Handle null values to prevent constructor exception
        if (decryptedUsername == null || decryptedUsername.isEmpty()) {
            throw new UsernameNotFoundException("Failed to decrypt username for user");
        }
        if (password == null || password.isEmpty()) {
            throw new UsernameNotFoundException("Invalid password for user");
        }
        
        return new org.springframework.security.core.userdetails.User(
                decryptedUsername,
                password,
                Collections.singleton(new SimpleGrantedAuthority(user.getRoles().get(0).getRoleName())));
    }
}
