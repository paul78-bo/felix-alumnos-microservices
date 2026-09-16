-- Inicialización de la base de datos alumnos-api-felix
-- Solo tablas esenciales: usuarios y alumnos

-- Tabla usuarios
CREATE TABLE IF NOT EXISTS `usuarios` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` varchar(20) NOT NULL,
  `enabled` tinyint(1) DEFAULT 1,
  `fecha_creacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Tabla alumnos
CREATE TABLE IF NOT EXISTS `alumnos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `carrera` varchar(100) DEFAULT NULL,
  `edad` int(11) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKrrq96s55g45kywh0ypeed2u1v` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT IGNORE INTO `usuarios` (`id`, `username`, `password`, `role`, `enabled`, `fecha_creacion`) VALUES
(7, 'admin', '$2a$10$jJoa5Mq.y0kKz40bF.aHmehePCLlJJMM.aAePH8Kcmvn6pZI1AXsU', 'ROLE_ADMIN', 1, '2025-09-30 23:19:45'),
(8, 'profesor', '$2a$10$tbtMTjdGDC7mX10oUbk0hUtjsOTsSxW8mw.KG7ecGgyXBmjRGRMK3u', 'ROLE_PROFESOR', 1, '2025-09-30 23:19:45'),
(9, 'alumno', '$2a$10$j7zei.H0e3fp8i8.Q50X..ffiaVj.1Xcz1HXocHgrzzrTQ5Cfl.LC', 'ROLE_ALUMNO', 1, '2025-09-30 23:19:45');



-- Insertar datos de ejemplo de alumnos
INSERT IGNORE INTO `alumnos` (`id`, `carrera`, `edad`, `email`, `nombre`) VALUES
(1, 'Ingeniería en Sistemas Computacionales', 30, 'ana.garcia@universidad.edu', 'Ana García López'),
(2, 'Medicina oncologica 6', 23, 'carlos.martinez@universidad.edu', 'Carlos Martínez Ruiz'),
(3, 'Derecho', 21, 'maria.rodriguez@universidad.edu', 'María Rodríguez Silva'),
(4, 'Administración de Empresas', 23, 'pedro.hernandez@universidad.edu', 'Pedro Hernández Castro'),
(7, 'Medicina', 28, 'maria@email.com', 'María García'),
(8, 'Derecho', 21, 'carlos@email.com', 'Carlos López Peral'),
(10, 'Ingeniería en Sistemas Computacionales', 48, 'iah@gmail.com', 'Chazan A H 2'),
(12, 'Lic. Educacion', 43, 'jaaviMonqui@gmail.com', 'Rosalia Cuenca Tapia');