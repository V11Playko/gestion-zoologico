package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Zona;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IZonaRepository extends JpaRepository<Zona, Long> {

    @EntityGraph(attributePaths = {"especies", "especies.animales"})
    @Query("SELECT z FROM Zona z")
    List<Zona> findAllWithEspeciesAndAnimales();
}
