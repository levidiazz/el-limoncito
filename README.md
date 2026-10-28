# El Limoncito

Plataforma integral de análisis comercial, control operativo y toma de decisiones para el negocio "El Limoncito" (abarrotes y cerveza al mayoreo y menudeo).

Equipo 7 · Gestión de Proyectos de Software · Instituto Tecnológico de Oaxaca

## Tecnologías

- Java 17
- Spring Boot 3 (Web, Data JPA, Security, Validation)
- Thymeleaf y Bootstrap 5
- PostgreSQL 18
- Flyway para crear y versionar la base de datos

## Arquitectura en 3 capas

| Capa | Paquete |
|---|---|
| Presentación | `controlador` y `templates` |
| Negocio | `servicio` |
| Datos | `repositorio` y `modelo` |

## Cómo ejecutar

1. Crear una base de datos vacía en PostgreSQL llamada `el_limoncito`.
2. Si el usuario o la contraseña de PostgreSQL no son `postgres`, definir las variables de entorno `DB_USUARIO` y `DB_CONTRASENA`.
3. Abrir el proyecto en IntelliJ IDEA o VS Code (con Extension Pack for Java) y ejecutar `ElLimoncitoApplication`, o en terminal `mvn spring-boot:run`. Flyway crea las tablas al iniciar.
4. Abrir http://localhost:8080

## Usuarios de prueba

Contraseña de todos: `Limoncito2026` (cambiarla en "Cambiar contraseña").

| Usuario | Rol |
|---|---|
| admin | Administrador |
| gerente | Gerente General |
| cajero | Ventas |
| almacen | Almacén |

## Sprint 2

| Módulo | Historias | Responsable |
|---|---|---|
| Proyecto base y envases retornables | HU-10.1 a HU-10.4 | José Ángel Antonio |
| Usuarios, roles y bitácora | HU-2.1 a HU-2.4 | Enlil Aparicio |
| Inicio de sesión | HU-1.1 a HU-1.4 | Irvin Montero |
| Categorías y productos | HU-5.1 a HU-5.4, HU-4.1 a HU-4.4 | Levi Pérez |
| Proveedores y entradas de mercancía | HU-6.1 a HU-6.4, HU-8.1 a HU-8.4 | Moisés Pascual |
| Existencias | HU-11.1 a HU-11.4 | Irvin Montero |
| Mermas | HU-9.1 a HU-9.4 | Ingrid Arcadio |
