# MiBombay — Sistema de barra

Sistema de venta en mostrador para restaurante/bar: catálogo de productos e
ingredientes, recetas con costeo automático y (en curso) venta rápida, cobro,
caja y reportes.

## Stack

| Tecnología      | Versión / detalle                          |
|-----------------|--------------------------------------------|
| Java            | 21                                         |
| Spring Boot     | 4.1.1                                      |
| Spring Web MVC  | Vistas server-side con Thymeleaf           |
| Spring Security | Login por formulario, roles                |
| Spring Data JPA | Hibernate, `ddl-auto=update` en desarrollo |
| Base de datos   | MySQL (desarrollo y producción)            |
| MapStruct       | DTOs y mapeo entidad/DTO                   |
| Lombok          | Entidades y DTOs                           |
| Bootstrap       | 5.3.3 por CDN, modo oscuro                 |

Paquete base: `com.mibombay.empresa`.

## Módulos y estado

| Módulo                          | Estado                                                              |
|---------------------------------|---------------------------------------------------------------------|
| Seguridad + usuarios            | Hecho (login, roles, semillas, gestión desde la app)                |
| Ingredientes                    | Hecho (catálogo, stock, valores, adicional)                         |
| Productos simples               | Hecho (categoría, stock, costo y precio)                            |
| Recetas + productos con receta  | Hecho (costo automático, líneas, precio de venta)                   |
| Venta rápida (comanda)          | Pendiente                                                           |
| Cobro                           | Pendiente                                                           |
| Caja (turnos, arqueo)           | Pendiente                                                           |
| Reportes + anulaciones          | Pendiente                                                           |

Las categorías de producto son un enum fijo (`BEBIDAS, CERVEZAS, LICORES,
COMIDA, POSTRES, EXTRAS`): no tienen CRUD ni tabla propia.

## Instalación

Requisitos: JDK 21, Maven (se incluye `mvnw`) y MySQL en `localhost:3306`.

```bash
git clone https://github.com/josegregoppdev/mibombayp.git
cd mibombayp   # o el nombre de la carpeta del proyecto
mysql -u root -p -e "CREATE DATABASE mibombayempresa;"
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
# editar application.properties con el nombre de BD y la contraseña reales
./mvnw spring-boot:run
```

Abrir `http://localhost:8080/login`.

Comandos habituales:

```bash
./mvnw -q compile   # compilar (verificación tras cada cambio)
./mvnw -q test      # tests
./mvnw -q package   # empaquetar
```

## Usuarios semilla y roles

Al arrancar se crean si no existen (contraseñas solo de desarrollo):

| Usuario | Clave     | Rol    | Acceso                                   |
|---------|-----------|--------|------------------------------------------|
| admin   | admin123  | ADMIN  | Total: catálogos, venta, configuración   |
| cajero  | cajero123 | CAJERO | Solo venta (`/venta/**`)                 |
| dev     | dev123    | DEV    | Gestión de catálogos y usuarios          |

Rutas de administración (`/admin/usuarios`, `/admin/ingredientes`,
`/admin/productos`, `/admin/recetas`, `/admin/productos-con-receta`):
roles `DEV` o `ADMIN`. El resto de `/admin/**`: solo `ADMIN`.

Reglas de negocio comunes: nunca se borra nada (baja con `activo=false`);
nombres únicos; precios de venta nunca menores al costo.

## Estructura del proyecto

```text
src/main/java/com/mibombay/empresa/
├── model/          # Entidades JPA
├── repository/     # Interfaces Spring Data JPA
├── service/        # Lógica de negocio (@Transactional solo aquí)
├── controller/     # Controladores MVC + Thymeleaf (devuelven vistas)
├── dto/            # DTOs (Usuario con Request/Response; resto con DTO único)
├── mapper/         # Interfaces MapStruct
├── config/         # Security, semillas de datos
└── util/           # ValidacionDatos (validación en service)

src/main/resources/
├── application.properties.example  # Plantilla (el .properties real no se sube)
├── templates/admin/<modulo>/        # Una subcarpeta por módulo
├── templates/fragments/             # head + header reutilizables
└── static/css|js                    # app.css (solo overrides mínimos)
```

## Convenciones

El archivo `AGENTS.md` documenta las convenciones del proyecto
(arquitectura, DTOs, validación en dos capas, manejo de excepciones, logs,
seguridad en dos capas, estilo Bootstrap y reglas por módulo). Los archivos
`*.txt` de bitácora y el `application.properties` real son locales y no se
suben al repositorio (ver `.gitignore`).
