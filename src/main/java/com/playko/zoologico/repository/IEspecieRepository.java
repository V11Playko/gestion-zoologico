package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Especie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IEspecieRepository extends JpaRepository<Especie, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
}
