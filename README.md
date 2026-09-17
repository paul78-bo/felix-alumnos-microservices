¡Perfecto! 🎯 Vamos a dejar el repo profesional con:

README.md profesional

Topics en GitHub

LICENSE

docs/ con capturas y diagramas

Colección Postman

📝 README profesional
Copia TODO este contenido y reemplaza el README.md actual:

text
=== INICIO DEL README ===

# 🎓 Felix Alumnos Microservices

Sistema de gestión académica construido con **arquitectura de microservicios** 
utilizando el stack moderno de **Spring Boot 3** y **Spring Cloud Gateway**.

> **🎯 Objetivo:**  
> Demostrar dominio de arquitectura de microservicios, seguridad con JWT, 
> API Gateway, y comunicación entre servicios en el ecosistema Spring.

---

## 📋 Tabla de Contenidos

- [Arquitectura](#-arquitectura)
- [Stack Tecnológico](#-stack-tecnológico)
- [Microservicios](#-microservicios)
- [Seguridad JWT](#-seguridad-jwt)
- [Cómo Ejecutar](#-cómo-ejecutar)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Capturas de Pantalla](#-capturas-de-pantalla)
- [Decisiones Técnicas](#-decisiones-técnicas)
- [Autor](#-autor)

---

## 🏗 Arquitectura

El sistema está compuesto por **3 microservicios** que se comunican a través 
de un **API Gateway** con validación JWT centralizada:
┌─────────────────────────────────────────────────────────────────┐
│ NAVEGADOR (Usuario) │
└─────────────────────────────────────────────────────────────────┘
│
│ HTTP :8082
▼
┌─────────────────────────────────────────────────────────────────┐
│ 🎨 Felix-microSFrontend (Spring Boot) → localhost:8082 │
│ - Frontend con HTML + JavaScript vanilla │
│ - Login, dashboard, gestión de alumnos/tareas/usuarios │
└─────────────────────────────────────────────────────────────────┘
│
│ HTTP :8080
▼
┌─────────────────────────────────────────────────────────────────┐
│ 🚪 Felix-API-GATEWAY (Spring Cloud Gateway) → localhost:8080 │
│ - Enrutamiento a microservicios │
│ - Validación JWT centralizada │
│ - CORS global │
│ - Agrega headers (X-User, X-Role) │
└─────────────────────────────────────────────────────────────────┘
│
│ HTTP :8081
▼
┌─────────────────────────────────────────────────────────────────┐
│ ⚙️ Felix-Alumnos-Service (Spring Boot + JPA) → localhost:8081│
│ - CRUD de Alumnos │
│ - CRUD de Tareas │
│ - CRUD de Usuarios │
│ - Autenticación (login + JWT) │
│ - Recuperación de contraseña por email │
│ - Soft delete │
│ - Subida de archivos │
│ - Swagger/OpenAPI │
└─────────────────────────────────────────────────────────────────┘
│
│ JDBC
▼
┌─────────────────────────────────────────────────────────────────┐
│ 🗄️ MySQL (puerto 3307) │
└─────────────────────────────────────────────────────────────────┘

text

### Flujo de una petición autenticada
Usuario hace login → Frontend envía POST /api/auth/login al Gateway

Gateway redirige → Alumnos Service valida credenciales

Alumnos Service genera JWT → lo devuelve al Frontend

Frontend guarda JWT en localStorage

Usuario pide datos → Frontend envía GET /api/alumnos con JWT en header

Gateway valida JWT → Si es válido, redirige al Alumnos Service

Alumnos Service responde con JSON → Gateway lo devuelve al Frontend

Frontend renderiza los datos

text

---

## 🛠 Stack Tecnológico

| Capa | Tecnología |
|------|------------|
| **Lenguaje** | Java 17 |
| **Framework** | Spring Boot 3.5 |
| **API Gateway** | Spring Cloud Gateway |
| **Seguridad** | Spring Security + JWT (JJWT) |
| **Persistencia** | Spring Data JPA + Hibernate 6.6 |
| **Base de Datos** | MySQL 8 |
| **Frontend** | HTML5 + JavaScript vanilla |
| **Documentación API** | SpringDoc OpenAPI (Swagger UI) |
| **Email** | Spring Mail (SMTP Gmail) |
| **Build** | Maven |
| **Contenedores** | Docker + Docker Compose |
| **Testing** | JUnit 5 |

---

## 📦 Microservicios

### 🚪 Felix-API-GATEWAY

**Puerto:** `8080`

**Responsabilidades:**
- Enrutamiento de peticiones a microservicios
- Validación JWT centralizada
- Configuración CORS global
- Agregar headers con información del usuario (`X-User`, `X-Role`)
- Rate limiting (si aplica)

**Rutas configuradas:**
- `/api/**` → Alumnos Service
- `/reset-password/**` → Alumnos Service
- `/login.html` → Frontend
- `/**` → Frontend

### ⚙️ Felix-Alumnos-Service

**Puerto:** `8081`

**Responsabilidades:**
- CRUD de **Alumnos**
- CRUD de **Tareas**
- CRUD de **Usuarios**
- **Autenticación** (login + generación de JWT)
- **Recuperación de contraseña** por email
- **Subida de archivos** (tareas)
- **Soft delete** en entidades principales
- **Swagger UI** para documentación
- **Rate limiting** en login

### 🎨 Felix-microSFrontend

**Puerto:** `8082`

**Responsabilidades:**
- Interfaz de usuario con HTML + JavaScript
- Login y registro
- Dashboard principal
- Gestión visual de alumnos, tareas y usuarios
- Recuperación de contraseña
- Perfil de usuario

---

## 🔐 Seguridad JWT

### Flujo de autenticación
LOGIN
Frontend → POST /api/auth/login {username, password}
Alumnos Service valida credenciales → genera JWT
Respuesta: {token: "eyJhbGciOiJIUzI1NiJ9..."}

ALMACENAMIENTO
Frontend guarda el JWT en localStorage

PETICIONES AUTENTICADAS
Frontend → GET /api/alumnos
Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

VALIDACIÓN EN GATEWAY
Gateway valida el JWT

Si es válido → redirige al microservicio

Si es inválido → 401 Unauthorized

AUTORIZACIÓN EN MICROSERVICIO
Alumnos Service valida el JWT de nuevo (defensa en profundidad)
Verifica roles y permisos

text

### Estructura del JWT

```json
{
  "sub": "ShadowMaster",
  "userId": 1,
  "role": "ROLE_ADMIN",
  "iat": 1789526908,
  "exp": 1789528708
}
🚀 Cómo Ejecutar
Opción A: Con Docker (recomendado)
Requisitos:

Docker Desktop instalado

Docker Compose

Pasos:

Clonar el repositorio

bash
git clone https://github.com/paul78-bo/felix-alumnos-microservices.git
cd felix-alumnos-microservices
Configurar variables de entorno

bash
cp .env.example .env
# Editar .env con tus credenciales
Levantar todos los servicios

bash
docker-compose up --build
Acceder a la aplicación

text
Frontend:   http://localhost:8082
Gateway:    http://localhost:8080
Backend:    http://localhost:8081
Swagger:    http://localhost:8081/swagger-ui.html
Opción B: Local (desarrollo)
Requisitos:

JDK 17

Maven 3.9+

MySQL 8

Pasos:

Clonar el repositorio

bash
git clone https://github.com/paul78-bo/felix-alumnos-microservices.git
cd felix-alumnos-microservices
Configurar MySQL

sql
CREATE DATABASE `alumnos-api-felix` CHARACTER SET utf8mb4;
Configurar credenciales

bash
# En cada microservicio:
cp src/main/resources/application.yml.example src/main/resources/application.yml
# Editar application.yml con tus credenciales
Levantar en orden

bash
# Terminal 1: Backend
cd Felix-Alumnos-Service
./mvnw spring-boot:run

# Terminal 2: Gateway
cd Felix-API-GATEWAY
./mvnw spring-boot:run

# Terminal 3: Frontend
cd Felix-microSFrontend
./mvnw spring-boot:run
Acceder

text
http://localhost:8082
📡 Endpoints de la API
🔐 Autenticación
Método	Endpoint	Descripción
POST	/api/auth/login	Login de usuario
POST	/api/auth/forgot-password	Solicitar recuperación
POST	/api/auth/reset-password	Restablecer contraseña
👥 Alumnos
Método	Endpoint	Descripción
GET	/api/alumnos	Listar todos
GET	/api/alumnos/paginado?page=0&size=10	Listar paginado
GET	/api/alumnos/{id}	Obtener por ID
POST	/api/alumnos	Crear
PUT	/api/alumnos/{id}	Actualizar
DELETE	/api/alumnos/{id}	Soft delete
📚 Tareas
Método	Endpoint	Descripción
GET	/api/tareas	Listar todas
GET	/api/tareas/paginado?page=0&size=10	Listar paginado
GET	/api/tareas/{id}	Obtener por ID
POST	/api/tareas	Crear
PUT	/api/tareas/{id}	Actualizar
DELETE	/api/tareas/{id}	Soft delete
👤 Usuarios
Método	Endpoint	Descripción
GET	/api/usuarios	Listar todos
GET	/api/usuarios/{id}	Obtener por ID
POST	/api/usuarios	Crear
PUT	/api/usuarios/{id}	Actualizar
DELETE	/api/usuarios/{id}	Eliminar
📖 Documentación completa: http://localhost:8081/swagger-ui.html

📸 Capturas de Pantalla
🔐 Login
https://docs/screenshots/login.png

📊 Dashboard
https://docs/screenshots/dashboard.png

👥 Gestión de Alumnos
https://docs/screenshots/alumnos.png

📚 Gestión de Tareas
https://docs/screenshots/tareas.png

👤 Gestión de Usuarios
https://docs/screenshots/usuarios.png

🤔 Decisiones Técnicas
¿Por qué microservicios?
✅ Separación de responsabilidades clara

✅ Escalabilidad independiente por servicio

✅ Despliegue independiente

✅ Tecnología heterogénea (cada servicio puede usar su stack)

✅ Equipos pequeños pueden trabajar en paralelo

¿Por qué Spring Cloud Gateway?
✅ Reactivo (basado en WebFlux/Netty)

✅ Rendimiento superior vs Zuul

✅ Integración nativa con Spring Security

✅ Filtros personalizables para JWT

✅ Configuración declarativa en YAML

¿Por qué JWT en lugar de sesiones?
✅ Stateless: no requiere almacenar sesiones en el servidor

✅ Escalable: cualquier instancia puede validar el token

✅ Multi-dispositivo: funciona en web, móvil, APIs

✅ Estándar de la industria para APIs REST

✅ Contiene información del usuario (claims)

¿Por qué soft delete?
✅ Preserva integridad referencial (FK constraints)

✅ Permite auditoría de registros eliminados

✅ Recuperación de datos

✅ Cumple con normativas de retención

¿Por qué separar Frontend del Backend?
✅ Desacoplamiento total

✅ El frontend puede cambiar sin tocar el backend

✅ El backend puede servir a múltiples clientes (web, móvil, etc.)

✅ Mejor testing por separado

👨‍💻 Autor
Felix Agustin Peral Garcia

🎓 Egresado de Licenciatura en Sistemas Computacionales e Informática

💼 Buscando oportunidades como Java Full Stack Developer.

📧 Email: paulponce56@gmail.com

💻 GitHub: @paul78-bo

🔗 LinkedIn: Felix Agustin Peral Garcia

📄 Licencia
Este proyecto está bajo la Licencia MIT. Ver el archivo LICENSE
para más detalles.

⭐ Si este proyecto te resultó útil, dale una estrella en GitHub! ⭐

