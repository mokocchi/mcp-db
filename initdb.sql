-- Script de inicialización para PostgreSQL
-- Define las tablas que consulta AlumnoRepository

CREATE TABLE IF NOT EXISTS alumnos (
    id BIGSERIAL PRIMARY KEY,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    curso VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS notas (
    id BIGSERIAL PRIMARY KEY,
    alumno_id BIGINT NOT NULL,
    materia VARCHAR(100) NOT NULL,
    nota NUMERIC(4,2) NOT NULL CHECK (nota >= 0 AND nota <= 10),
    CONSTRAINT fk_notas_alumno
        FOREIGN KEY (alumno_id)
        REFERENCES alumnos(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_nota_alumno_materia UNIQUE (alumno_id, materia)
);

-- Datos de ejemplo para probar la aplicación
INSERT INTO alumnos (dni, nombre, apellido, curso)
VALUES
    ('12345678', 'Ana', 'García', 'DAW2'),
    ('87654321', 'Luis', 'Pérez', 'DAM1')
ON CONFLICT (dni) DO NOTHING;

INSERT INTO notas (alumno_id, materia, nota)
SELECT id, 'Matemáticas', 8.5
FROM alumnos
WHERE dni = '12345678'
ON CONFLICT (alumno_id, materia) DO NOTHING;

INSERT INTO notas (alumno_id, materia, nota)
SELECT id, 'Programación', 9.2
FROM alumnos
WHERE dni = '87654321'
ON CONFLICT (alumno_id, materia) DO NOTHING;
