package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Especie;
import org.hibernate.boot.model.source.spi.EmbeddableSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface IEspecieRepository extends JpaRepository<Especie, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<Especie> findByNombreIgnoreCase(String nombre);
}
