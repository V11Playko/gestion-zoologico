package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    List<Usuario> findAllById(Long idUser);
    boolean existsByEmailIgnoreCase(String email);
}
