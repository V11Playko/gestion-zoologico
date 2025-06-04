package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByAnimalAndPadreIsNullOrderByFechaAsc(Animal animal);
    boolean existsByAnimal_Id(Long animalId);
}
