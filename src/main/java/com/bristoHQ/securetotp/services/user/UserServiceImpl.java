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
import com.bristoHQ.securetotp.helper.EncryptionService;
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
    private EncryptionService encryptionService;

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

        if (iUserRepository.existsByEmail(encryptionService.encrypt(registerDto.getEmail()))) {
            System.out.println("User with email already exists: " + registerDto.getEmail());
            return new ResponseEntity<>("Email is already taken!", HttpStatus.SEE_OTHER);
        }
        if (iUserRepository.existsByUsername(encryptionService.encrypt(registerDto.getUsername()))) {
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
        if (registerDto.getProvider().equals("GOOGLE")) {
            user.setVerified(true); // Automatically verify Google users
        } else {
            user.setVerified(false); // Default to non-verified for other providers
        }
        // Automatically verify on registration
        user.setPremium(false); // Default to non-premium
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        // Assign role with username
        Role role = new Role(RoleName.USER, user.getUsername());
        System.out.println("Saving role: " + role);
        saveRole(role);

        user.setRoles(Collections.singletonList(role));
        System.out.println("Saving user: " + user);
        saveUser(encryptUser(user));

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
                    encryptionService.encrypt(loginDto.getEmailOrUsername()),
                    encryptionService.encrypt(loginDto.getEmailOrUsername())).isPresent();

            if (!userExists) {
                throw new UsernameNotFoundException(
                        "User not found with email/username: " + loginDto.getEmailOrUsername());
            }

            // Attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getEmailOrUsername(),
                            loginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get the authenticated user
            User user = iUserRepository
                    .findByEmailOrUsername(encryptionService.encrypt(authentication.getName()),
                            encryptionService.encrypt(authentication.getName()))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Decrypt user details before generating token
            user = decryptUser(user);
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
        User user = iUserRepository.findByEmailOrUsername(encryptionService.encrypt(emailOrUsername),
                encryptionService.encrypt(emailOrUsername)).isPresent()
                        ? iUserRepository.findByEmailOrUsername(encryptionService.encrypt(emailOrUsername),
                                encryptionService.encrypt(emailOrUsername)).get()
                        : null;

        return dtoService.convertUserToUserDTO(decryptUser(user));
    }

    @Override
    public UserDTO findByEmail(String email) {
        System.out.println("Finding user by email: " + encryptionService.encrypt(email));
        User user = iUserRepository.findByEmail(encryptionService.encrypt(email)).isPresent()
                ? iUserRepository.findByEmail(encryptionService.encrypt(email)).get()
                : null;
        System.out.println("User found: " + user);
        return dtoService.convertUserToUserDTO(decryptUser(user));
    }

    @Override
    public UserDTO findByUsername(String username) {
        User user = iUserRepository.findByUsername(encryptionService.encrypt(username)).isPresent()
                ? iUserRepository.findByUsername(encryptionService.encrypt(username)).get()
                : null;
        return dtoService.convertUserToUserDTO(decryptUser(user));
    }

    @Override
    public UserDTO findByEmailOrUsername(String email, String username) {
        return dtoService
                .convertUserToUserDTO(decryptUser(iUserRepository
                        .findByEmailOrUsername(encryptionService.encrypt(email), encryptionService.encrypt(username))
                        .isPresent()
                                ? iUserRepository.findByEmailOrUsername(encryptionService.encrypt(email),
                                        encryptionService.encrypt(username)).get()
                                : null));
    }

    @Override
    public List<UserDTO> getAllUsers() {
        if (iUserRepository.findAll().isEmpty()) {
            return null;
        }
        return dtoService.convertUserToUserDTO(
                decryptUsers(iUserRepository.findAll() != null ? iUserRepository.findAll() : Collections.emptyList()));
    }

    @Override
    public UserDTO findById(String id) {
        return dtoService
                .convertUserToUserDTO(
                        decryptUser(iUserRepository.findById(id).orElse(null)));
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
        return iUserRepository.findByUsernameAndIsPremium(encryptionService.encrypt(username), true).isPresent();
    }

    @Override
    public boolean isUserPremiumByEmail(String email) {
        return iUserRepository.findByEmailAndIsPremium(encryptionService.encrypt(email), true).isPresent();
    }

    @Override
    public boolean isUserPremiumByEmailOrUsername(String email, String username) {
        return iUserRepository.findByEmailOrUsernameAndIsPremium(encryptionService.encrypt(email),
                encryptionService.encrypt(username), true).isPresent();
    }

    @Override
    public boolean isOAuthUser(String emailOrUsername) {
        User user = iUserRepository
                .findByEmailOrUsername(encryptionService.encrypt(emailOrUsername),
                        encryptionService.encrypt(emailOrUsername))
                .orElse(null);

        if (user == null) {
            return false;
        }

        String provider = user.getProvider();
        // Check if provider is not null and not LOCAL (assuming LOCAL means regular
        // login)
        return provider != null && !provider.equalsIgnoreCase("LOCAL");
    }

    @Override
    public String newJwtToken(String emailOrUsername) {
        User user = iUserRepository.findByEmailOrUsername(encryptionService.encrypt(emailOrUsername),
                encryptionService.encrypt(emailOrUsername)).get();
        List<String> rolesNames = new ArrayList<>();
        user = decryptUser(user); // Decrypt user details before generating token
        user.getRoles().forEach(r -> rolesNames.add(r.getRoleName()));
        return jwtUtilities.generateToken(user.getUsername(), rolesNames);
    }

    @Override
    public UserDTO updateUsername(String oldUsername, String newUsername) {
        if (iUserRepository.findByUsername(encryptionService.encrypt(newUsername)).isPresent()) {
            return null;
        }
        Optional<User> user = iUserRepository.findByUsername(encryptionService.encrypt(oldUsername));
        if (user.isPresent()) {
            user.get().setUsername(newUsername);
            updateUsernameInRole(oldUsername, newUsername);
            saveUser(encryptUser(user.get()));
            return dtoService.convertUserToUserDTO(decryptUser(user.get()));
        }
        return null;
    }

    @Override
    public UserDTO updateEmail(String usernameOrEmail, String newEmail) {

        if (iUserRepository.findByEmail(encryptionService.encrypt(newEmail)).isPresent()) {
            return null;
        }
        Optional<User> user = iUserRepository.findByEmailOrUsername(encryptionService.encrypt(usernameOrEmail),
                encryptionService.encrypt(usernameOrEmail));
        if (user.isPresent()) {
            user.get().setEmail(encryptionService.encrypt(newEmail));
            saveUser(user.get());
            return dtoService.convertUserToUserDTO(user.get());
        }
        return null;
    }

    @Override
    public Role updateUsernameInRole(String oldUsername, String username) {
        Optional<User> user = iUserRepository.findByUsername(encryptionService.encrypt(oldUsername));
        if (user.isPresent()) {
            Role role = user.get().getRoles().get(0);
            role.setUsername(username);
            return iRoleRepository.save(role);
        }
        return null;
    }

    public void updatePassword(String email, String newPassword) {
        Optional<User> userOptional = iUserRepository.findByEmailOrUsername(encryptionService.encrypt(email),
                encryptionService.encrypt(email));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            iUserRepository.save(user);  // Save directly since user is already encrypted
        } else {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }

    public boolean checkPassword(String usernameOrEmail, String currentPassword){
        User user = iUserRepository.findByEmailOrUsername(encryptionService.encrypt(usernameOrEmail),
                encryptionService.encrypt(usernameOrEmail)).orElse(null);
        if (user == null || user.getPassword() == null) {
            return false;
        }
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    // public boolean saveNewPassword(String usernameOrEmail, String newPassword) {
    //     User user = iUserRepository.findByEmailOrUsername(encryptionService.encrypt(usernameOrEmail),
    //             encryptionService.encrypt(usernameOrEmail)).orElse(null);
    //     if (user == null) {
    //         return false; // User not found
    //     }
    //     user.setPassword(passwordEncoder.encode(newPassword));
    //     iUserRepository.save(encryptUser(user));
    //     return true;
    // }

    @Override
    public void updateUserDetails(String username, UserProfileUpdateDTO updatedUser) {
        Optional<User> userOptional = iUserRepository.findByUsername(encryptionService.encrypt(username));

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user = decryptUser(user); // Decrypt user details before updating

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

            if (updatedUser.getRecoveryPhone() != null) {
                user.setRecoveryPhone(updatedUser.getRecoveryPhone());
            }
            if (updatedUser.getRecoveryEmail() != null) {
                user.setRecoveryEmail(updatedUser.getRecoveryEmail());
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
            saveUser(encryptUser(user));
        }
    }

    @Override
    public boolean isUserExist(String email) {
        return iUserRepository.existsByEmail(encryptionService.encrypt(email));
    }

    public boolean isUserExistByUsername(String username) {
        return iUserRepository.existsByUsername(encryptionService.encrypt(username));
    }

    @SuppressWarnings("unused")
    private List<UserDTO> encryptUsersDTO(List<UserDTO> users) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService is not initialized");
        }
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserDTO> encryptedUsers = new ArrayList<>();
        for (UserDTO user : users) {
            encryptedUsers.add(encryptUser(user));
        }
        return encryptedUsers;
    }

    @SuppressWarnings("unused")
    private List<User> encryptUsers(List<User> users) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService is not initialized");
        }
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        List<User> encryptedUsers = new ArrayList<>();
        for (User user : users) {
            encryptedUsers.add(encryptUser(user));
        }
        return encryptedUsers;
    }

    @SuppressWarnings("unused")
    private List<UserDTO> decryptUsersDTO(List<UserDTO> users) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService is not initialized");
        }
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        List<UserDTO> decryptedUsers = new ArrayList<>();
        for (UserDTO user : users) {
            decryptedUsers.add(decryptUser(user));
        }
        return decryptedUsers;
    }

    private List<User> decryptUsers(List<User> users) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService is not initialized");
        }
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        List<User> decryptedUsers = new ArrayList<>();
        for (User user : users) {
            decryptedUsers.add(decryptUser(user));
        }
        return decryptedUsers;
    }

    private User encryptUser(User user) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService is not initialized");
        }
        if (user == null) {
            return null;
        }
        User encryptedUser = new User();
        encryptedUser.setId(user.getId());
        encryptedUser.setFullName(null != user.getFullName() ? encryptionService.encrypt(user.getFullName()) : null);
        encryptedUser.setUsername(null != user.getUsername() ? encryptionService.encrypt(user.getUsername()) : null);
        encryptedUser.setEmail(null != user.getEmail() ? encryptionService.encrypt(user.getEmail()) : null);
        encryptedUser.setPassword(user.getPassword()); // Password should not be encrypted
        encryptedUser.setRoles(user.getRoles());
        encryptedUser.setProvider(user.getProvider());
        encryptedUser.setPremium(user.isPremium());
        encryptedUser.setRedeemCode(user.getRedeemCode());

        encryptedUser.setAccountCreatedAt(user.getAccountCreatedAt());
        encryptedUser.setVerified(user.isVerified());
        encryptedUser.setOtp(null != user.getOtp() ? encryptionService.encrypt(user.getOtp()) : null);
        encryptedUser.setOtpGeneratedTime(user.getOtpGeneratedTime());
        encryptedUser.setUserAvatar(
                null != user.getUserAvatar() ? encryptionService.encrypt(user.getUserAvatar()) : null);
        encryptedUser.setUserAvatarpublicId(
                null != user.getUserAvatarpublicId() ? encryptionService.encrypt(user.getUserAvatarpublicId())
                        : null);
        encryptedUser.setUserBanner(
                null != user.getUserBanner() ? encryptionService.encrypt(user.getUserBanner()) : null);
        encryptedUser.setUserBannerpublicId(
                null != user.getUserBannerpublicId() ? encryptionService.encrypt(user.getUserBannerpublicId())
                        : null);
        return encryptedUser;
    }

    private User decryptUser(User user) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService is not initialized");
        }
        if (user == null) {
            return null;
        }
        User decryptedUser = new User();
        decryptedUser.setId(user.getId());
        decryptedUser.setFullName(null != user.getFullName() ? encryptionService.decrypt(user.getFullName()) : null);
        decryptedUser.setUsername(null != user.getUsername() ? encryptionService.decrypt(user.getUsername()) : null);
        decryptedUser.setEmail(null != user.getEmail() ? encryptionService.decrypt(user.getEmail()) : null);
        decryptedUser.setPassword(user.getPassword()); // Password should not be decrypted
        decryptedUser.setRoles(user.getRoles());
        decryptedUser.setProvider(user.getProvider());
        decryptedUser.setPremium(user.isPremium());
        decryptedUser.setRedeemCode(user.getRedeemCode());

        decryptedUser.setAccountCreatedAt(user.getAccountCreatedAt());
        decryptedUser.setVerified(user.isVerified());
        decryptedUser.setOtp(null != user.getOtp() ? encryptionService.decrypt(user.getOtp()) : null);
        decryptedUser.setOtpGeneratedTime(user.getOtpGeneratedTime());
        decryptedUser.setUserAvatar(
                null != user.getUserAvatar() ? encryptionService.decrypt(user.getUserAvatar()) : null);
        decryptedUser.setUserAvatarpublicId(
                null != user.getUserAvatarpublicId() ? encryptionService.decrypt(user.getUserAvatarpublicId())
                        : null);
        decryptedUser.setUserBanner(
                null != user.getUserBanner() ? encryptionService.decrypt(user.getUserBanner()) : null);
        decryptedUser.setUserBannerpublicId(
                null != user.getUserBannerpublicId() ? encryptionService.decrypt(user.getUserBannerpublicId())
                        : null);
        return decryptedUser;
    }

    private UserDTO encryptUser(UserDTO userDTO) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService is not initialized");
        }
        if (userDTO == null) {
            return null;
        }
        UserDTO encryptedUser = new UserDTO();
        encryptedUser.setId(userDTO.getId());
        encryptedUser
                .setFullName(null != userDTO.getFullName() ? encryptionService.encrypt(userDTO.getFullName()) : null);
        encryptedUser
                .setUsername(null != userDTO.getUsername() ? encryptionService.encrypt(userDTO.getUsername()) : null);
        encryptedUser.setEmail(null != userDTO.getEmail() ? encryptionService.encrypt(userDTO.getEmail()) : null);
        encryptedUser.setRoles(userDTO.getRoles());
        encryptedUser.setProvider(userDTO.getProvider());
        encryptedUser.setPremium(userDTO.isPremium());
        encryptedUser.setRedeemCode(userDTO.getRedeemCode());

        encryptedUser.setAccountCreatedAt(userDTO.getAccountCreatedAt());
        encryptedUser.setVerified(userDTO.isVerified());
        encryptedUser.setOtp(null != userDTO.getOtp() ? encryptionService.encrypt(userDTO.getOtp()) : null);
        encryptedUser.setOtpGeneratedTime(userDTO.getOtpGeneratedTime());
        encryptedUser.setUserAvatar(
                null != userDTO.getUserAvatar() ? encryptionService.encrypt(userDTO.getUserAvatar()) : null);
        encryptedUser.setUserAvatarpublicId(
                null != userDTO.getUserAvatarpublicId() ? encryptionService.encrypt(userDTO.getUserAvatarpublicId())
                        : null);
        encryptedUser.setUserBanner(
                null != userDTO.getUserBanner() ? encryptionService.encrypt(userDTO.getUserBanner()) : null);
        encryptedUser.setUserBannerpublicId(
                null != userDTO.getUserBannerpublicId() ? encryptionService.encrypt(userDTO.getUserBannerpublicId())
                        : null);
        return encryptedUser;
    }

    private UserDTO decryptUser(UserDTO userDTO) {
        if (encryptionService == null) {
            throw new IllegalStateException("EncryptionService is not initialized");
        }
        if (userDTO == null) {
            return null;
        }
        UserDTO decryptedUser = new UserDTO();
        decryptedUser.setId(userDTO.getId());
        decryptedUser
                .setFullName(null != userDTO.getFullName() ? encryptionService.decrypt(userDTO.getFullName()) : null);
        decryptedUser
                .setUsername(null != userDTO.getUsername() ? encryptionService.decrypt(userDTO.getUsername()) : null);
        decryptedUser.setEmail(null != userDTO.getEmail() ? encryptionService.decrypt(userDTO.getEmail()) : null);
        decryptedUser.setRoles(userDTO.getRoles());
        decryptedUser.setProvider(userDTO.getProvider());
        decryptedUser.setPremium(userDTO.isPremium());
        decryptedUser.setRedeemCode(userDTO.getRedeemCode());

        decryptedUser.setAccountCreatedAt(userDTO.getAccountCreatedAt());
        decryptedUser.setVerified(userDTO.isVerified());
        decryptedUser.setOtp(null != userDTO.getOtp() ? encryptionService.decrypt(userDTO.getOtp()) : null);
        decryptedUser.setOtpGeneratedTime(userDTO.getOtpGeneratedTime());
        decryptedUser.setUserAvatar(
                null != userDTO.getUserAvatar() ? encryptionService.decrypt(userDTO.getUserAvatar()) : null);
        decryptedUser.setUserAvatarpublicId(
                null != userDTO.getUserAvatarpublicId() ? encryptionService.decrypt(userDTO.getUserAvatarpublicId())
                        : null);
        decryptedUser.setUserBanner(
                null != userDTO.getUserBanner() ? encryptionService.decrypt(userDTO.getUserBanner()) : null);
        decryptedUser.setUserBannerpublicId(
                null != userDTO.getUserBannerpublicId() ? encryptionService.decrypt(userDTO.getUserBannerpublicId())
                        : null);
        return decryptedUser;
    }
}