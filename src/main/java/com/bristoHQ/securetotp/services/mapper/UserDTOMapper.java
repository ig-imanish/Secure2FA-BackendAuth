package com.bristoHQ.securetotp.services.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bristoHQ.securetotp.dto.user.UserDTO;
import com.bristoHQ.securetotp.models.User;

@Service
public class UserDTOMapper {

    public UserDTO convertUserToUserDTO(User user) {

        if (user == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();

        if (user.getId() != null) {
            userDTO.setId(user.getId());
        }
        if (user.getFullName() != null) {
            userDTO.setFullName(user.getFullName());
        }
        if (user.getUsername() != null) {
            userDTO.setUsername(user.getUsername());
        }
        if (user.getEmail() != null) {
            userDTO.setEmail(user.getEmail());
        }
        if (user.getRoles() != null) {
            userDTO.setRoles(user.getRoles());
        }
        if (user.getProvider() != null && !user.getProvider().isEmpty()) {
            userDTO.setProvider(user.getProvider());
        }

        // Add isPremium field
        userDTO.setPremium(user.isPremium());

        if (user.getRedeemCode() != null) {
            userDTO.setRedeemCode(user.getRedeemCode());
        }

        // Remove duplicate check - we already set roles above
        // if(!user.getRoles().isEmpty()) {
        // userDTO.setRoles(user.getRoles());
        // }

        if (user.getAccountCreatedAt() != null) {
            userDTO.setAccountCreatedAt(user.getAccountCreatedAt());
        }

        // Add verified status
        userDTO.setVerified(user.isVerified());

        // Add OTP fields
        if (user.getOtp() != null) {
            userDTO.setOtp(user.getOtp());
        }

        if (user.getOtpGeneratedTime() != null) {
            userDTO.setOtpGeneratedTime(user.getOtpGeneratedTime());
        }

        if (user.getUserAvatar() != null) {
            userDTO.setUserAvatar(user.getUserAvatar());
        }

        if (user.getUserAvatarpublicId() != null) {
            userDTO.setUserAvatarpublicId(user.getUserAvatarpublicId());
        }

        if (user.getUserBanner() != null) {
            userDTO.setUserBanner(user.getUserBanner());
        }

        if (user.getUserBannerpublicId() != null) {
            userDTO.setUserBannerpublicId(user.getUserBannerpublicId());
        }

        return userDTO;
    }

    public User convertUserDTOToUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User();

        if (userDTO.getId() != null) {
            user.setId(userDTO.getId());
        }
        if (userDTO.getFullName() != null) {
            user.setFullName(userDTO.getFullName());
        }
        if (userDTO.getUsername() != null) {
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getRoles() != null) {
            user.setRoles(userDTO.getRoles());
        }
        if (userDTO.getProvider() != null && !userDTO.getProvider().isEmpty()) {
            user.setProvider(userDTO.getProvider());
        }

        // Set isPremium field
        user.setPremium(userDTO.isPremium());

        if (userDTO.getRedeemCode() != null) {
            user.setRedeemCode(userDTO.getRedeemCode());
        }

        if (userDTO.getAccountCreatedAt() != null) {
            user.setAccountCreatedAt(userDTO.getAccountCreatedAt());
        }

        // Set verified status
        user.setVerified(userDTO.isVerified());

        // Set OTP fields
        if (userDTO.getOtp() != null) {
            user.setOtp(userDTO.getOtp());
        }

        if (userDTO.getOtpGeneratedTime() != null) {
            user.setOtpGeneratedTime(userDTO.getOtpGeneratedTime());
        }

        // Set avatar and banner fields
        if (userDTO.getUserAvatar() != null) {
            user.setUserAvatar(userDTO.getUserAvatar());
        }

        if (userDTO.getUserAvatarpublicId() != null) {
            user.setUserAvatarpublicId(userDTO.getUserAvatarpublicId());
        }

        if (userDTO.getUserBanner() != null) {
            user.setUserBanner(userDTO.getUserBanner());
        }

        if (userDTO.getUserBannerpublicId() != null) {
            user.setUserBannerpublicId(userDTO.getUserBannerpublicId());
        }

        return user;
    }

    public List<UserDTO> convertUserToUserDTO(List<User> users) {
        if (users == null) {
            return List.of();
        }

        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            userDTOs.add(convertUserToUserDTO(user));
        }
        return userDTOs;
    }
}
