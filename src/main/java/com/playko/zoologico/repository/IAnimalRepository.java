package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findByNombreIgnoreCase(String nombre);
    boolean existsByEspecie(Especie especie);
    boolean existsByEspecie_Zona(Zona zona);
    List<Animal> findByFechaIngresoBetween(LocalDateTime inicio, LocalDateTime fin);


}
