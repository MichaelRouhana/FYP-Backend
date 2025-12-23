package com.example.AzureTestProject.Api.Repository;


import com.example.AzureTestProject.Api.Entity.UserRole;
import com.example.AzureTestProject.Api.Model.Constant.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<UserRole, Long> {

    Optional<UserRole> findByRole(Role role);

}