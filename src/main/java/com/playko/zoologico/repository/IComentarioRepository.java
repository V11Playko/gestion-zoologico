package com.playko.zoologico.repository;

import com.playko.zoologico.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IComentarioRepository extends JpaRepository<Comentario, Long> {
}
