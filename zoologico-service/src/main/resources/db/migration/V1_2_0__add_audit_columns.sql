-- ========================================
-- AGREGAR COLUMNAS DE AUDITORÍA A TODAS LAS TABLAS
-- ========================================

-- Tabla: usuarios
ALTER TABLE usuarios
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
ADD COLUMN updated_at TIMESTAMP,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN created_by BIGINT,
ADD COLUMN updated_by BIGINT,
ADD COLUMN deleted_by BIGINT;

ALTER TABLE usuarios
ADD CONSTRAINT fk_usuarios_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
ADD CONSTRAINT fk_usuarios_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id),
ADD CONSTRAINT fk_usuarios_deleted_by FOREIGN KEY (deleted_by) REFERENCES usuarios(id);

-- Tabla: zonas
ALTER TABLE zonas
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
ADD COLUMN updated_at TIMESTAMP,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN created_by BIGINT,
ADD COLUMN updated_by BIGINT,
ADD COLUMN deleted_by BIGINT;

ALTER TABLE zonas
ADD CONSTRAINT fk_zonas_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
ADD CONSTRAINT fk_zonas_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id),
ADD CONSTRAINT fk_zonas_deleted_by FOREIGN KEY (deleted_by) REFERENCES usuarios(id);

-- Tabla: especies
ALTER TABLE especies
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
ADD COLUMN updated_at TIMESTAMP,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN created_by BIGINT,
ADD COLUMN updated_by BIGINT,
ADD COLUMN deleted_by BIGINT;

ALTER TABLE especies
ADD CONSTRAINT fk_especies_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
ADD CONSTRAINT fk_especies_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id),
ADD CONSTRAINT fk_especies_deleted_by FOREIGN KEY (deleted_by) REFERENCES usuarios(id);

-- Tabla: animales (CON AUDITORÍA, SIN creador_id)
ALTER TABLE animales
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
ADD COLUMN updated_at TIMESTAMP,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN created_by BIGINT,
ADD COLUMN updated_by BIGINT,
ADD COLUMN deleted_by BIGINT;

ALTER TABLE animales
ADD CONSTRAINT fk_animales_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
ADD CONSTRAINT fk_animales_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id),
ADD CONSTRAINT fk_animales_deleted_by FOREIGN KEY (deleted_by) REFERENCES usuarios(id);

-- Tabla: comentarios
ALTER TABLE comentarios
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW(),
ADD COLUMN updated_at TIMESTAMP,
ADD COLUMN deleted_at TIMESTAMP,
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN created_by BIGINT,
ADD COLUMN updated_by BIGINT,
ADD COLUMN deleted_by BIGINT;

ALTER TABLE comentarios
ADD CONSTRAINT fk_comentarios_created_by FOREIGN KEY (created_by) REFERENCES usuarios(id),
ADD CONSTRAINT fk_comentarios_updated_by FOREIGN KEY (updated_by) REFERENCES usuarios(id),
ADD CONSTRAINT fk_comentarios_deleted_by FOREIGN KEY (deleted_by) REFERENCES usuarios(id);

-- ========================================
-- ÍNDICES PARA MEJORA DE PERFORMANCE
-- ========================================

-- Índices para soft delete (consultas comunes filtran por deleted=false)
CREATE INDEX idx_usuarios_deleted ON usuarios(deleted) WHERE deleted = false;
CREATE INDEX idx_zonas_deleted ON zonas(deleted) WHERE deleted = false;
CREATE INDEX idx_especies_deleted ON especies(deleted) WHERE deleted = false;
CREATE INDEX idx_animales_deleted ON animales(deleted) WHERE deleted = false;
CREATE INDEX idx_comentarios_deleted ON comentarios(deleted) WHERE deleted = false;

-- Índices para auditoría (reportes y consultas de usuarios)
CREATE INDEX idx_animales_created_by ON animales(created_by);
CREATE INDEX idx_comentarios_autor ON comentarios(autor_id);