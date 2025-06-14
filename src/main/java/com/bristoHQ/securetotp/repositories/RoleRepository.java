package com.bristoHQ.securetotp.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bristoHQ.securetotp.models.role.Role;
import com.bristoHQ.securetotp.models.role.RoleName;

public interface RoleRepository extends MongoRepository<Role, Integer> {

    Role findByRoleName(String name);

    List<Role> findByRoleName(RoleName name);

    Role findById(Long id);
}
