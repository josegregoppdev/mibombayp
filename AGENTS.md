# AGENTS.md

## Proyecto
Sistema de barra (venta en mostrador) para restaurante/bar — Spring Boot **4.1.1**, Java **21**, paquete base `com.mibombay.empresa`.

- **Arquitectura: MVC server-side** (Spring Web MVC + Thymeleaf), capas:
  - `model/` → entidades JPA (persistencia y dominio)
  - `repository/` → interfaces Spring Data JPA
  - `service/` → lógica de negocio (aquí vive lo importante, no en los controladores)
  - `controller/` → controladores Spring MVC que devuelven vistas Thymeleaf
  - `config/` → configuración (Security, semillas de datos)
- **Base de datos:** MySQL con JPA/Hibernate (`ddl-auto=update` en desarrollo).
- **Seguridad:** Spring Security 7 con login por formulario y roles `ADMIN`, `CAJERO` y `DEV`.
- **Datos de conexión y usuarios semilla:** `datos_app.txt` (rellenar nombre de BD y contraseña ahí; las contraseñas van cifradas con BCrypt, nunca en texto plano en la BD).

## Cómo trabajamos
- **Módulo a módulo, bajo petición del usuario.** No implementar módulos que no se hayan pedido.
- Orden previsto:
  1. Seguridad + usuarios (login, roles, semillas) ← hecha (base; cada módulo suma sus reglas URL + `@PreAuthorize`)
  2. Menú (categorías y productos, CRUD admin) ← parcial: productos simples hechos (categoría como enum `Categoria`, sin CRUD ni tabla de categorías; con `stockActual/stockMinimo` + `costo/precioVenta`); productos con receta hechos (módulo separado, ver 9)
  3. Venta rápida (comanda en pantalla)
  4. Cobro (métodos de pago, guardado de venta)
  5. Caja (apertura/cierre de turno, arqueo)
  6. Reportes de ventas + anulaciones con motivo
  7. Gestión de usuarios desde la app ← hecha (listado, alta, edición, activar/desactivar)
  8. Ingredientes (catálogo + stock, CRUD admin) ← hecho (listado, alta, edición, activar/desactivar)
  9. Recetas + productos con receta ← hecho (Receta con nombre + costo auto = suma cantidad*valorCompra; DetalleReceta ingrediente+cantidad+unidad con unique(receta,ingrediente); ProductoConReceta 1-a-1 con receta + precioVenta >= costo; sin borrado físico)
- Tras cada cambio: compilar (`./mvnw -q compile`) antes de dar por terminada la tarea.
- **Tests módulo a módulo, junto con el módulo:** cada módulo nuevo sumo sus tests con la misma convención (ver sección Tests). Hoy cubierto: `util/ValidacionDatos` completo (35 tests) + `UsuarioService.listar()` (2 tests, Mockito).

## Comandos
- Compilar: `./mvnw -q compile`
- Tests: `./mvnw test` (corre tests + genera reporte JaCoCo en `target/site/jacoco/index.html`)
- Verificar (tests + regla de cobertura 90% controller/service): `./mvnw verify` → **ver NOTA importante de la sección Tests**
- Ejecutar: `./mvnw spring-boot:run`
- Empaquetar: `./mvnw -q package` (NO aplica la regla de cobertura; `verify` va después de `package`)

No hay linter/formatter configurado; la verificación es compilación + tests de Maven.

## Estructura de archivos
```
src/main/java/com/mibombay/empresa/
├── model/          # @Entity (Usuario, Ingrediente, Producto, Receta, DetalleReceta, ProductoConReceta, ...)
├── repository/     # interfaces JpaRepository
├── service/        # @Service, lógica de negocio
├── controller/     # @Controller MVC + Thymeleaf
├── dto/            # DTOs (Usuario con Request/Response; resto con DTO único) — Lombok
├── mapper/         # interfaces MapStruct (@Mapper(componentModel = "spring"))
├── config/         # SecurityConfig, DataInitializer, etc.
└── util/           # ValidacionDatos (validación de entradas, métodos static)

src/main/resources/
├── application.properties   # datasource MySQL
├── templates/               # vistas Thymeleaf (login.html, home.html, ...)
│   ├── admin/               # una subcarpeta por módulo: usuarios/, ingredientes/, ... (cada módulo con sus vistas dentro)
│   │   ├── usuarios/        # usuarios.html (listado), usuario-form.html (alta/edición)
│   │   ├── ingredientes/    # ingredientes.html (listado), ingrediente-form.html (alta/edición)
│   │   ├── productos/       # productos.html (listado), producto-form.html (alta/edición)
│   │   ├── recetas/         # recetas.html (listado), receta-form.html (alta/edición + líneas)
│   │   ├── productos-con-receta/ # productos-con-receta.html, producto-con-receta-form.html
│   │   └── <modulo>/        # futuros módulos siguen el mismo patrón
│   └── fragments/           # fragments reutilizables (head.html, header.html)
└── static/css|js            # recursos estáticos (app.css = overrides Bootstrap)

src/test/java/com/mibombay/empresa/   # tests JUnit 5 (misma estructura que src/main: config/, controller/, dto/, ..., util/)
                                      # TestValidacionDatos = 35 tests (suite completa, sin BD)
src/test/resources/                   # NO existe todavía (los tests @SpringBootTest usan la config real y tocan MySQL)
```

## Convenciones
- Indentación con **tabs** (igual que el código existente).
- Clases de dominio en singular (`Usuario`, `Producto`); tablas en snake_case con `@Table(name = "...")`.
- Lombok en entidades/DTOs (`@Getter`, `@Setter`, `@NoArgsConstructor`…); el annotation processor ya está configurado en el `pom.xml`.
- Contraseñas: siempre `BCryptPasswordEncoder`; nunca texto plano ni en logs.
- Acceso a datos: vía `service` → `repository`; los controladores no hacen consultas directas a JPA.
- **Transacciones solo en `service`:** `@Transactional` en escrituras (`crear/actualizar/alternarActivo`); `@Transactional(readOnly = true)` en lecturas (`listar/obtenerPorId/existe*`, `loadUserByUsername`). Nada de `@Transactional` en controladores.
- **DTOs con MapStruct:** controladores y services solo ven clases de `dto/` (reciben y devuelven DTO); la entidad `Usuario` no sale de `service`/`repository`. Los mappers viven en `mapper/` como interfaz `@Mapper(componentModel = "spring")` (Spring usa la generada `*MapperImpl`). El password solo aparece en `*DTORequest`: **nunca** en un `*DTOResponse` ni en logs. Si un mapeo necesita ignorar un campo, usar `@Mapping(target = "...", ignore = true)`.
- **DTO único (regla general):** `Producto`, `Ingrediente`, `Receta`, `DetalleReceta` y `ProductoConReceta` usan un solo `*DTO` para request y response (sin sufijos) porque no tienen campos sensibles como el password; solo `Usuario` mantiene Request/Response separados. Los mappers de DTO único exponen `toDTO/toDTOList/toEntity` (sin `toRequest`).
- Validación de entradas: **dos capas**.
  1. **Controlador** → `@Valid` + `BindingResult` sobre los DTOs (`*DTORequest`, `*DTO`) con anotaciones `jakarta.validation` (`spring-boot-starter-validation` en el pom). Si `resultado.hasErrors()` se re-renderiza la vista con `th:errors` / `th:errorclass="is-invalid"` (sin redirect).
  2. **Service** → siempre con `util/ValidacionDatos.*` (métodos static que lanzan `IllegalArgumentException`); los services no escriben `if (x == null || x.isBlank())` a mano. Las reglas que consultan la BD (p. ej. username duplicado) sí se quedan en el service. Numéricos (`stock`, `valorCompra/valorVenta`) con `ValidacionDatos.stock(...)` (no nulo + `>= 0`); la regla `valorVenta >= valorCompra` vive en `IngredienteService.validarMargen(...)`.
- **Sin try/catch en controladores**: las excepciones de negocio (`IllegalArgumentException`, `NoSuchElementException`) las captura `controller/GlobalExceptionHandler` (`@ControllerAdvice`): flash attribute `error` + redirect al Referer (solo paths de la app, evita open redirect; fallback `/`). Las vistas lo pintan con `th:if="${error}"`.
- **Logs con SLF4J (`LoggerFactory`):** solo `debug/info/warn` (sin `error` en negocio). `debug` para entradas `GET` y fallos de `@Valid` (`resultado.getErrorCount()`, sin dumpear campos); `info` para mutaciones OK con `id + username + rol` (p. ej. `Usuario creado: id={} username={} rol={}`); `warn` solo en `GlobalExceptionHandler` (no duplicar en service/controller). Siempre con `{}` sin concatenar y nunca `password`/hash.
- Vistas Thymeleaf: usar `th:action` y `sec:authorize` (dependencia `thymeleaf-extras-springsecurity6`) para el menú según rol.
- Vistas por módulo: cada módulo tiene su subcarpeta (`templates/admin/<modulo>/`, `templates/venta/<modulo>/`, ...); no van vistas sueltas directo bajo `admin/` o `venta/`.
- Tests: `Test` como prefijo del nombre de la clase bajo prueba (`TestUsuarioService`); métodos de test en camelCase; estructura Given/When/Then con comentarios y solo `Assertions` de JUnit 5.

## Tests y cobertura (JaCoCo)
- **Plugin JaCoCo 0.8.12** en el `pom.xml`: `prepare-agent` + `report` (fase `test`) + `check` (fase `verify`).
- **Regla del check:** `LINE >= 90%` SOLO sobre `com.mibombay.empresa.controller.*` y `com.mibombay.empresa.service.*` (el resto del proyecto no cuenta para la regla).
- **Reporte HTML:** `target/site/jacoco/index.html` (se genera con `./mvnw test` o `./mvnw verify`).
- **Convención de tests** (ya aplicada en `TestValidacionDatos`):
  1. Clase `Test` + clase bajo prueba (`TestValidacionDatos`, futuro `TestUsuarioService`).
  2. Métodos camelCase: `metodo_escenario_resultado` (`requerido_valorNulo_lanzaExcepcion`).
  3. Estructura Given/When/Then con comentarios.
  4. Etiqueta de camino arriba de cada `@Test`: `// Camino feliz ...`, `// Null ...`, `// Excepción ...`.
  5. Tests de services/controllers: JUnit 5 `Assertions` + **Mockito** (`@ExtendWith(MockitoExtension.class)`, `@Mock` para dependencias, `@InjectMocks` para el service bajo prueba). Tests de `util/` siguen solo-Assertions (sin mocks).
  6. Cubrir: camino feliz, null y excepción (y listas/parámetros externos si aparecen).
  7. **Datos falsos:** clase `DataProvider<Clase>` en la carpeta espejo (p. ej. `service/DataProviderUsuario.java`), solo métodos estáticos con constructor privado, que fabrica entidades/DTOs para los stubs (`usuarioValido()`, `listaUsuarios()`, `requestValido()`, ...).
- **Alta de tests de un módulo nuevo:** crear la clase de test en la carpeta espejo (`src/test/java/com/mibombay/empresa/<paquete>/`) junto con el propio módulo, misma convención.
- ✅ **RESUELTO (2026-10-01) — el falso verde de `./mvnw verify`:** la causa era una **mala configuración del plugin JaCoCo en el `pom.xml`** (el propio plugin estaba "bugueado": los `includes` no matcheaban ninguna clase y JaCoCo trataba la regla vacía como cumplida). Se corrigió poniendo `<element>CLASS</element>` con los `includes` de `controller.*`, `service.*` y `util.*`, y ahora el check **evalúa clase a clase y falla como corresponde** (BUILD FAILURE listando cada clase por debajo del 90%). El check ya es fiable como puerta de release.

## Fragments (Thymeleaf)
Para no repetir código en cada HTML, todo lo común vive en `src/main/resources/templates/fragments/`:

- **`fragments/head.html`** → `th:fragment="head(titulo)"`: charset, viewport, CSS de Bootstrap (CDN), `app.css` y `<title>` parametrizado.
  - Uso: `<head th:replace="~{fragments/head :: head('Inicio')}"></head>`
- **`fragments/header.html`** → `th:fragment="cabecera"`: navbar con marca (link `<a>` a `/` = retorno al home), usuario (`sec:authentication`), badge de rol y botón Salir.
  - Uso: `<header th:replace="~{fragments/header :: cabecera}"></header>`

Reglas:
- Toda página nueva (admin, venta, etc.) usa **ambos fragments** y solo escribe su `<main>`.
- `xmlns:sec` solo se declara en las páginas que usan `sec:authorize`/`sec:authentication`.
- No duplicar el `<head>` ni la cabecera a mano; si hace falta algo nuevo, se añade al fragment.

## Colores y estilo (Bootstrap)
- **Bootstrap 5.3.3 por CDN** (incluido en `fragments/head.html`: CSS + `bootstrap.bundle.min.js`). No hay dependencia en el `pom.xml`.
- **Modo oscuro obligatorio:** cada página lleva `<html lang="es" data-bs-theme="dark">` y `body` con `bg-body-secondary`.
- **Paleta:**
  - Acento principal: **ámbar** → `btn-warning`, bordes `border-warning-subtle`.
  - Venta/acciones secundarias: **verde** → `btn-success`, `border-success-subtle`.
  - Textos secundarios: `text-body-secondary`; badges de rol con `badge text-bg-secondary`.
- **`static/css/app.css` es solo para overrides mínimos** (~5 líneas). No crear estilos propios que Bootstrap ya cubra (inputs, cards, alertas, navbar, etc. usan clases Bootstrap).
- Alertas de mensajes: `alert alert-danger` (error) y `alert alert-success` (ok), con `th:if="${param.error}"` / `${param.logout}`.

## Reglas de acceso (seguridad)
- **Seguridad en dos capas, se mantienen ambas:**
  1. **Por URL** → `SecurityConfig.securityFilterChain(...)` (`authorizeHttpRequests`).
  2. **Por método** → `@EnableMethodSecurity` en `SecurityConfig` + `@PreAuthorize` en controladores y servicios.
- Públicos: `/login`, `/css/**`, `/js/**`, `/error`
- `/admin/usuarios` y `/admin/usuarios/**` → `ROLE_DEV` o `ROLE_ADMIN` (regla **antes** de `/admin/**`)
- `/admin/ingredientes` y `/admin/ingredientes/**` → `ROLE_DEV` o `ROLE_ADMIN` (regla **antes** de `/admin/**`)
- `/admin/productos` y `/admin/productos/**` → `ROLE_DEV` o `ROLE_ADMIN` (regla **antes** de `/admin/**`)
- `/admin/**` → solo `ROLE_ADMIN`
- `/venta/**` → `ROLE_ADMIN` o `ROLE_CAJERO`
- Cualquier otra URL → autenticado
- Login con formulario propio en `/login`; logout en `/logout`; CSRF activo.
- `@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")` en `UsuarioAdminController` (clase) y en `UsuarioService.listar/obtenerPorId/actualizar/alternarActivo`.
- **`UsuarioService.crearUsuario()` NO lleva `@PreAuthorize`**: lo llama `DataInitializer` al arrancar sin sesión autenticada y se bloquearía el arranque.
- `@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")` en `IngredienteAdminController` (clase) y en **todos** los métodos de `IngredienteService` (ningún seeder lo llama sin sesión).
- `@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")` en `ProductoAdminController` (clase) y en **todos** los métodos de `ProductoService` (ningún seeder lo llama sin sesión).
- `@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")` en `RecetaAdminController` y `ProductoConRecetaAdminController` (clase) y en **todos** los métodos de `RecetaService` y `ProductoConRecetaService`.
- `/admin/recetas` y `/admin/recetas/**` → `ROLE_DEV` o `ROLE_ADMIN` (regla **antes** de `/admin/**`)
- `/admin/productos-con-receta` y `/admin/productos-con-receta/**` → `ROLE_DEV` o `ROLE_ADMIN` (regla **antes** de `/admin/**`)
- Semillas en `DataInitializer` (se crean solo si no existen): `admin/admin123`, `cajero/cajero123`, `dev/dev123`.
- Regla de negocio: nunca se borran usuarios (baja = `activo=false`) y no se puede desactivar al único ADMIN activo.
- Reglas de ingrediente: nunca se borran (baja = `activo=false`), `nombre` único (insensible a mayúsculas), `valorCompra/valorVenta` nunca nulos ni negativos y `valorVenta >= valorCompra`. `esAdicional` por defecto `true` (checkbox en el form + badge en el listado). Si la tabla ya tenía filas, tras añadir las columnas rellenar con `UPDATE ingrediente SET valor_compra=0, valor_venta=0, es_adicional=true WHERE ... IS NULL`.
- Reglas de producto: nunca se borran (baja = `activo=false`), `nombre` único global (insensible a mayúsculas), `categoria` como enum `Categoria` (sin CRUD ni tabla; valores `BEBIDAS, CERVEZAS, LICORES, COMIDA, POSTRES, EXTRAS`), `stockActual/stockMinimo` (`BigDecimal` 12,3, nunca nulos ni negativos) y `costo/precioVenta` nunca nulos ni negativos con `precioVenta >= costo`. Son los productos simples (sin receta; los compuestos viven en `producto_con_receta`). Si la tabla ya tenía filas antes de añadir columnas, rellenar con `UPDATE producto SET stock_actual=0, stock_minimo=0 WHERE stock_actual IS NULL OR stock_minimo IS NULL` (igual que ingredientes).
- Reglas de receta: nunca se borran (baja = `activo=false`), `nombre` único (insensible a mayúsculas), `costo` automático = suma `cantidad * ingrediente.valorCompra` (se recalcula al agregar/quitar líneas, nunca se edita a mano). `DetalleReceta` con `ingrediente+cantidad>0+unidadMedida`, unique `(receta,ingrediente)`, ingrediente debe estar activo. Decisión 2026-09-30: sin snapshot de valor en el detalle (costo en vivo desde `valorCompra` actual).
- Reglas de producto con receta: nunca se borran, `nombre` único, relación 1-a-1 con `Receta` (una receta solo en un producto), `precioVenta >= receta.costo` (el admin fija el precio viendo el costo).

## Notas y riesgos
- **Tests:** hay 36 (35 de `TestValidacionDatos` sin BD + 1 `EmpresaApplicationTests` con `@SpringBootTest` que conecta a MySQL). La verificación diaria es `./mvnw -q compile` + `./mvnw test`. `./mvnw verify` aplica la regla de cobertura del 90% (ya fiable, ver sección "Tests y cobertura") y fallará hasta que existan tests de services/controllers.
- Si falta config de datasource o la BD no existe, `spring-boot:run` fallará.
- Tras el login, `/admin` y `/venta` pueden devolver 404 hasta que se implementen esos módulos: es esperado.
- El `pom.xml` tiene elementos vacíos heredados (`<licenses>`, `<developers>`, `<scm>`) — no rellenar salvo que se pida.
- Documentación de arranque de Spring en `HELP.md`.
- Próximo trabajo (inventario + config global + validador POS): `pendiente_2026-09-29.txt` (nada implementado; decisiones: sin PIN, `controlaInventario` boolean solo-ADMIN en Producto e Ingrediente, caché en memoria pendiente de explicar).
- Git + GitHub activos desde 2026-09-30: repo `josegregoppdev/mibombayp`, rama `main`, trabajo por ramas/commits. No se suben `*.txt` ni `application.properties` (ver `.gitignore`); la plantilla es `application.properties.example`.
