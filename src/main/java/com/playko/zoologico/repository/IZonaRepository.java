package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Zona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface IZonaRepository extends JpaRepository<Zona, Long> {

    @Query("SELECT z FROM Zona z WHERE (:nombre IS NULL OR LOWER(z.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) ORDER BY z.nombre ASC")
    Page<Zona> findZonasPaginadas(
            @Param("nombre") String nombre,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"especies", "especies.animales"})
    @Query("SELECT DISTINCT z FROM Zona z LEFT JOIN FETCH z.especies e LEFT JOIN FETCH e.animales WHERE z.id IN :ids ORDER BY z.nombre ASC")
    List<Zona> findZonasWithFetchByIds(@Param("ids") List<Long> ids);
}
