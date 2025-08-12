package com.playko.zoologico.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "zonas")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre;

    @OneToMany(mappedBy = "zona", fetch = FetchType.LAZY)
    @OrderBy("nombre ASC")
    private Set<Especie> especies = new LinkedHashSet<>();
}
