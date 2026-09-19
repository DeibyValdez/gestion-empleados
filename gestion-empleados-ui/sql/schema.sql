-- utf8mb4 para guardar bien tildes y ñ (Ana Lucía, Pérez, etc.).
CREATE DATABASE IF NOT EXISTS empresa_empleados
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE empresa_empleados;

SET NAMES utf8mb4;

-- Una sola tabla: cada fila es un empleado.
CREATE TABLE IF NOT EXISTS empleados (

    -- El sistema asigna el id solo y nunca se repite: PK autoincremental.
    id                 INT           NOT NULL AUTO_INCREMENT,

    -- Nombre completo. 100 caracteres alcanzan para nombres y apellidos largos.
    -- NOT NULL porque el nombre no puede quedar vacío.
    nombre             VARCHAR(100)  NOT NULL,

    -- Texto libre (la empresa no tiene una lista cerrada de departamentos),
    -- por eso VARCHAR y no una tabla aparte. 50 caracteres son suficientes.
    departamento       VARCHAR(50)   NOT NULL,

    -- Dinero con centavos: DECIMAL guarda el valor exacto (FLOAT/DOUBLE
    -- pueden dar errores de redondeo). DECIMAL(10,2) = hasta 8 enteros y
    -- 2 decimales, más que suficiente para un salario mensual.
    salario            DECIMAL(10,2) NOT NULL,

    -- Solo importa el día de contratación, no la hora: DATE.
    fecha_contratacion DATE          NOT NULL,
    
    -- Años completos de experiencia del empleado. No puede ir negativo.
    anios_experiencia INT			NOT NULL DEFAULT 0,

    -- Un empleado que se retira NO se borra, solo deja de estar activo.
    -- BOOLEAN. Un empleado nuevo nace activo.
    activo             BOOLEAN       NOT NULL DEFAULT TRUE,

    PRIMARY KEY (id),

    -- Regla de negocio: el salario debe ser mayor a cero (ni cero ni negativo).
    CONSTRAINT chk_empleados_salario CHECK (salario > 0),
    CONSTRAINT chk_empleados_anios_experiencia CHECK (anios_experiencia >= 0)
) ENGINE = InnoDB;

-- Datos de prueba. Ejecuta estos INSERT una sola vez;
-- si corres el script otra vez se duplicarán los 3 empleados.
INSERT INTO empleados (nombre, departamento, salario, fecha_contratacion, anios_experiencia, activo) VALUES
    ('Ana Lucía Pérez',     'Sistemas',     8500.00, '2024-03-15', 2, TRUE),
    ('Carlos Roberto Mux',  'Ventas',       6200.00, '2023-07-01', 3, TRUE),
    ('Diana Sofía Cabrera', 'Contabilidad', 7100.00, '2022-01-10', 8, FALSE);