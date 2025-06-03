package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IZonaRepository extends JpaRepository<Zona, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}
