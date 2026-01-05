package com.playko.zoologico.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "zonas")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SQLDelete(sql = "UPDATE zonas SET deleted = true, deleted_at = now() WHERE id = ?")
@Where(clause = "deleted = false")
public class Zona extends Audit{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;

    @OneToMany(mappedBy = "zona", fetch = FetchType.LAZY)
    @OrderBy("nombre ASC")
    private Set<Especie> especies = new LinkedHashSet<>();
}
