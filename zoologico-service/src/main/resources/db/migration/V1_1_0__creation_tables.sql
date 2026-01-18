CREATE TABLE role (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(255) UNIQUE NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE usuarios (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    id_role BIGINT NOT NULL,
    CONSTRAINT fk_usuario_role 
        FOREIGN KEY (id_role) REFERENCES role(id)
);

CREATE TABLE zonas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE especies (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    zona_id BIGINT NOT NULL,
    CONSTRAINT fk_especie_zona 
        FOREIGN KEY (zona_id) REFERENCES zonas(id)
);

CREATE TABLE animales (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    fecha_ingreso TIMESTAMP NOT NULL DEFAULT NOW(),
    especie_id BIGINT NOT NULL,
    CONSTRAINT fk_animal_especie 
        FOREIGN KEY (especie_id) REFERENCES especies(id)
);

CREATE TABLE comentarios (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    contenido VARCHAR(1000) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    animal_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    padre_id BIGINT,
    CONSTRAINT fk_comentario_animal 
        FOREIGN KEY (animal_id) REFERENCES animales(id),
    CONSTRAINT fk_comentario_autor 
        FOREIGN KEY (autor_id) REFERENCES usuarios(id),
    CONSTRAINT fk_comentario_padre 
        FOREIGN KEY (padre_id) REFERENCES comentarios(id)
);