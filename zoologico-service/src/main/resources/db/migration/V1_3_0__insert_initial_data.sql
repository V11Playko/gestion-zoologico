INSERT INTO role (nombre, descripcion)
VALUES
    ('ROLE_ADMIN', 'Administrador del sistema'),
    ('ROLE_EMPLEADO', 'Empleado del zoológico'),
    ('ROLE_CLIENTE', 'Cliente del sistema')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO usuarios (id, nombre, email, password, id_role, created_at)
OVERRIDING SYSTEM VALUE
VALUES (
    1,
    'Admin Sistema',
    'admin@zoologico.com',  -- ✅ Email genérico del sistema
    '$2a$10$DOWSDjR1gJ4YwVY2Z3tL2O5lZJ7vF9LkF6mO9x4Qm0Qw5W5Yk6X5W',
    (SELECT id FROM role WHERE nombre = 'ROLE_ADMIN'),
    NOW()
)
ON CONFLICT (id) DO NOTHING;

SELECT setval('usuarios_id_seq', GREATEST(1, (SELECT MAX(id) FROM usuarios)));

INSERT INTO zonas (nombre, created_by, created_at)
VALUES
    ('Sabana', 1, NOW()),
    ('Selva', 1, NOW()),
    ('Acuático', 1, NOW()),
    ('Desierto', 1, NOW()),
    ('Ártico', 1, NOW())
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO especies (nombre, zona_id, created_by, created_at)
SELECT especie, z.id, 1, NOW()
FROM (VALUES 
    ('León', 'Sabana'),
    ('Elefante', 'Sabana'),
    ('Tigre', 'Selva'),
    ('Mono', 'Selva'),
    ('Delfín', 'Acuático'),
    ('Pingüino', 'Ártico')
) AS data(especie, zona_nombre)
JOIN zonas z ON z.nombre = data.zona_nombre
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO animales (nombre, fecha_ingreso, especie_id, created_by, created_at)
SELECT animal, NOW(), e.id, 1, NOW()
FROM (VALUES 
    ('Simba', 'León'),
    ('Dumbo', 'Elefante')
) AS data(animal, especie_nombre)
JOIN especies e ON e.nombre = data.especie_nombre;