package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Animal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findByNombreIgnoreCase(String nombre);

}
