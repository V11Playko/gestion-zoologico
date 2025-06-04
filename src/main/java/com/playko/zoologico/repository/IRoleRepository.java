package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRoleRepository extends JpaRepository<Role, Long> {
    Role findByNombre(String nombre);
    Optional<Role> findByNombreIgnoreCase(String nombre);
}
