package com.bristoHQ.securetotp.services.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bristoHQ.securetotp.dto.auth.BearerToken;
import com.bristoHQ.securetotp.dto.auth.LoginDto;
import com.bristoHQ.securetotp.dto.auth.RegisterDto;
import com.bristoHQ.securetotp.dto.user.UserDTO;
import com.bristoHQ.securetotp.dto.user.UserProfileUpdateDTO;
import com.bristoHQ.securetotp.models.User;
import com.bristoHQ.securetotp.models.auth.BlacklistedToken;
import com.bristoHQ.securetotp.models.role.Role;
import com.bristoHQ.securetotp.models.role.RoleName;
import com.bristoHQ.securetotp.repositories.BlacklistedTokenRepository;
import com.bristoHQ.securetotp.repositories.RoleRepository;
import com.bristoHQ.securetotp.repositories.UserRepository;
import com.bristoHQ.securetotp.security.jwt.JwtUtilities;
import com.bristoHQ.securetotp.services.mapper.UserDTOMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository iUserRepository;
    @Autowired
    private RoleRepository iRoleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtilities jwtUtilities;

    @Autowired
    private UserDTOMapper dtoService;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    public Role saveRole(Role role) {
        return iRoleRepository.save(role);
    }

    @Override
    public UserDTO saveUser(User user) {
        return dtoService.convertUserToUserDTO(iUserRepository.save(user));
    }

    @Override
    public UserDTO saveUser(UserDTO user) {
        return dtoService.convertUserToUserDTO(iUserRepository.save(dtoService.convertUserDTOToUser(user)));
    }

    @Override
    public ResponseEntity<?> register(RegisterDto registerDto) {
        System.out.println("Register method called with: " + registerDto);

        if (iUserRepository.existsByEmail(registerDto.getEmail())) {
            System.out.println("User with email already exists: " + registerDto.getEmail());
            return new ResponseEntity<>("Email is already taken!", HttpStatus.SEE_OTHER);
        }
        if (iUserRepository.existsByUsername(registerDto.getUsername())) {
            System.out.println("Username already exists: " + registerDto.getUsername());
            return new ResponseEntity<>("Username is already taken!", HttpStatus.SEE_OTHER);
        }

        if (!registerDto.getUsername().startsWith("@")) {
            registerDto.setUsername("@" + registerDto.getUsername());
        }

        User user = new User();
        user.setEmail(registerDto.getEmail());
        user.setFullName(registerDto.getFullName());
        user.setUsername(registerDto.getUsername());
        user.setProvider(registerDto.getProvider());
        user.setAccountCreatedAt(new Date());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        // Assign role with username
        Role role = new Role(RoleName.USER, user.getUsername());
        System.out.println("Saving role: " + role);
        iRoleRepository.save(role);

        user.setRoles(Collections.singletonList(role));
        System.out.println("Saving user: " + user);
        iUserRepository.save(user);

        System.out.println("User saved successfully: " + user);

        String token = jwtUtilities.generateToken(registerDto.getEmail(),
                Collections.singletonList(role.getRoleName()));
        return new ResponseEntity<>(new BearerToken(token, "Bearer "), HttpStatus.OK);
    }

    @Override
    public String authenticate(LoginDto loginDto) {
        try {
            // First check if user exists before attempting authentication
            boolean userExists = iUserRepository.findByEmailOrUsername(
                    loginDto.getEmailOrUsername(), 
                    loginDto.getEmailOrUsername()).isPresent();
                    
            if (!userExists) {
                throw new UsernameNotFoundException("User not found with email/username: " + loginDto.getEmailOrUsername());
            }
            
            // Attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmailOrUsername(),
                            loginDto.getPassword()));
                            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Get the authenticated user
            User user = iUserRepository.findByEmailOrUsername(authentication.getName(), authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                    
            // Extract roles and generate token
            List<String> rolesNames = new ArrayList<>();
            user.getRoles().forEach(r -> rolesNames.add(r.getRoleName()));
            System.out.println("Generating token for user: " + user.getUsername() + " with roles: " + rolesNames);
            String token = jwtUtilities.generateToken(user.getUsername(), rolesNames);
            return token;
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // This specifically catches wrong password errors
            throw new RuntimeException("Incorrect password", e);
        } catch (UsernameNotFoundException e) {
            // This catches user not found errors
            throw e;
        } catch (Exception e) {
            // Generic error handling
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    @Override
    public UserDTO getUserDetails(String actualToken) {
        String emailOrUsername = jwtUtilities.extractUsername(actualToken);
        if (emailOrUsername == null || emailOrUsername.isEmpty()) {
            throw new UsernameNotFoundException("User not found with token: " + actualToken);
        }

        return dtoService.convertUserToUserDTO(
                iUserRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername).isPresent() ? iUserRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername).get() : null);
    }

    @Override
    public UserDTO findByEmail(String email) {
        return dtoService.convertUserToUserDTO(
                iUserRepository.findByEmail(email).isPresent() ? iUserRepository.findByEmail(email).get() : null);
    }

    @Override
    public UserDTO findByUsername(String username) {
        return dtoService.convertUserToUserDTO(
                iUserRepository.findByUsername(username).isPresent() ? iUserRepository.findByUsername(username).get()
                        : null);
    }

    @Override
    public UserDTO findByEmailOrUsername(String email, String username) {
        return dtoService.convertUserToUserDTO(iUserRepository.findByEmailOrUsername(email, username).isPresent()
                ? iUserRepository.findByEmailOrUsername(email, username).get()
                : null);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        if (iUserRepository.findAll().isEmpty()) {
            return null;
        }
        return dtoService.convertUserToUserDTO(iUserRepository.findAll());
    }

    @Override
    public UserDTO findById(Long id) {
        return dtoService
                .convertUserToUserDTO(iUserRepository.findById(id) == null ? null : iUserRepository.findById(id));
    }

    @Override
    public void logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
    }

    @Override
    public void blacklistToken(String token) {
        Claims claims = jwtUtilities.extractAllClaims(token);
        Instant expiration = claims.getExpiration().toInstant();
        BlacklistedToken blacklistedToken = new BlacklistedToken(token, expiration);
        blacklistedTokenRepository.save(blacklistedToken);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    @Override
    public boolean isUserPremiumByUsername(String username) {
        return iUserRepository.findByUsernameAndIsPremium(username, true).isPresent();
    }

    @Override
    public boolean isUserPremiumByEmail(String email) {
        return iUserRepository.findByEmailAndIsPremium(email, true).isPresent();
    }

    @Override
    public boolean isUserPremiumByEmailOrUsername(String email, String username) {
        return iUserRepository.findByEmailOrUsernameAndIsPremium(email, username, true).isPresent();
    }

    @Override
    public boolean isOAuthUser(String emailOrUsername) {
        User user = iUserRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername)
                .orElse(null);
        
        if (user == null) {
            return false;
        }
        
        String provider = user.getProvider();
        // Check if provider is not null and not LOCAL (assuming LOCAL means regular login)
        return provider != null && !provider.equalsIgnoreCase("LOCAL");
    }

    @Override
    public String newJwtToken(String emailOrUsername) {
        User user = iUserRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername).get();
        List<String> rolesNames = new ArrayList<>();
        user.getRoles().forEach(r -> rolesNames.add(r.getRoleName()));
        return jwtUtilities.generateToken(user.getUsername(), rolesNames);
    }

    @Override
    public UserDTO updateUsername(String oldUsername, String newUsername) {
        if (iUserRepository.findByUsername(newUsername).isPresent()) {
            return null;
        }
        Optional<User> user = iUserRepository.findByUsername(oldUsername);
        if (user.isPresent()) {
            user.get().setUsername(newUsername);
            updateUsernameInRole(newUsername);
            saveUser(user.get());
            return dtoService.convertUserToUserDTO(user.get());
        }
        return null;
    }

    @Override
    public UserDTO updateEmail(String usernameOrEmail, String newEmail) {
        if (iUserRepository.findByEmail(newEmail).isPresent()) {
            return null;
        }
        Optional<User> user = iUserRepository.findByEmailOrUsername(usernameOrEmail, usernameOrEmail);
        if (user.isPresent()) {
            user.get().setEmail(newEmail);
            saveUser(user.get());
            return dtoService.convertUserToUserDTO(user.get());
        }
        return null;
    }

    @Override
    public Role updateUsernameInRole(String username) {
        Optional<User> user = iUserRepository.findByUsername(username);
        if (user.isPresent()) {
            Role role = user.get().getRoles().get(0);
            role.setUsername(user.get().getUsername());
            return iRoleRepository.save(role);
        }
        return null;
    }

    @Override
    public void updateUserDetails(String username, UserProfileUpdateDTO updatedUser) {
        Optional<User> userOptional = iUserRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (updatedUser.getFullName() != null) {
                user.setFullName(updatedUser.getFullName());
            }
            if (updatedUser.getUsername() != null) {
                user.setUsername(updatedUser.getUsername());
            }
            if (updatedUser.getEmail() != null) {
                user.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getUserAvatar() != null) {
                user.setUserAvatar(updatedUser.getUserAvatar());
            }
            if (updatedUser.getUserBanner() != null) {
                user.setUserBanner(updatedUser.getUserBanner());
            }
            if (updatedUser.getBio() != null) {
                user.setBio(updatedUser.getBio());
            }
            if (updatedUser.getCountryName() != null) {
                user.setCountryName(updatedUser.getCountryName());
            }
            if (updatedUser.getCity() != null) {
                user.setCity(updatedUser.getCity());
            }
            if (updatedUser.getRecoveryPhone() != null) {
                user.setRecoveryPhone(updatedUser.getRecoveryPhone());
            }
            if (updatedUser.getRecoveryEmail() != null) {
                user.setRecoveryEmail(updatedUser.getRecoveryEmail());
            }
            if (updatedUser.getSocialLinks() != null) {
                user.setSocialLinks(updatedUser.getSocialLinks());
            }
            if (updatedUser.getJobTitle() != null) {
                user.setJobTitle(updatedUser.getJobTitle());
            }
            if (updatedUser.getCompany() != null) {
                user.setCompany(updatedUser.getCompany());
            }
            if (updatedUser.getWebsite() != null) {
                user.setWebsite(updatedUser.getWebsite());
            }
            if (updatedUser.getBirthDate() != null) {
                user.setBirthDate(updatedUser.getBirthDate());
            }
            if (updatedUser.getGender() != null) {
                user.setGender(updatedUser.getGender());
            }

            if (updatedUser.getUserAvatar() != null) {
                user.setUserAvatar(updatedUser.getUserAvatar());
            }
            if (updatedUser.getUserAvatarpublicId() != null) {
                user.setUserAvatarpublicId(updatedUser.getUserAvatarpublicId());
            }

            if (updatedUser.getUserBanner() != null) {
                user.setUserBanner(updatedUser.getUserBanner());
            }
            if (updatedUser.getUserBannerpublicId() != null) {
                user.setUserBannerpublicId(updatedUser.getUserBannerpublicId());
            }

            System.out.println("User updated: " + user);
            user.setProfileUpdatedAt(new Date());
            iUserRepository.save(user);
        }
    }

    @Override
    public boolean isUserExist(String email){
        return iUserRepository.existsByEmail(email);
    }
    public boolean isUserExistByUsername(String username){
        return iUserRepository.existsByUsername(username);
    }
}
