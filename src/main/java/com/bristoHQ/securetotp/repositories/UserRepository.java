package com.bristoHQ.securetotp.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bristoHQ.securetotp.models.User;
import com.bristoHQ.securetotp.models.role.Role;
import com.bristoHQ.securetotp.models.role.RoleName;

public interface UserRepository extends MongoRepository<User, String> {

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    List<User> findByRoles(List<Role> roles);

    Boolean existsByUsername(String username);

    Boolean existsByEmailOrUsername(String email, String username);

    List<User> findByIsPremium(boolean isPremium);

    Optional<User> findByUsernameAndIsPremium(String username, boolean isPremium);

    Optional<User> findByEmailAndIsPremium(String email, boolean isPremium);

    Optional<User> findByEmailOrUsernameAndIsPremium(String email, String username, boolean isPremium);

    // Analytics methods
    Long countByLastActiveAtAfter(Date date);

    Long countByAccountCreatedAtAfter(Date date);

    Long countByAccountCreatedAtBetween(Date startDate, Date endDate);

    Long countByLastActiveAtBetween(Date startDate, Date endDate);

    Long countByIsPremium(boolean isPremium);

    Long countByVerified(boolean verified);

    Long countByRolesRoleName(RoleName roleName);
}
