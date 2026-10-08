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
  9. Recetas + productos con receta ← hecho (Receta con nombre + costo auto = suma cantidad*valorCompra; DetalleReceta ingrediente+cantidad con unique(receta,ingrediente), unidad = la del ingrediente, no editable; ProductoConReceta 1-a-1 con receta + precioVenta >= costo; sin borrado físico)
  10. Clientes ← hecho (2026-10-08; CRUD admin simple: nombre/nombre jurídico + DNI/NIT obligatorios, apellido/dirección/teléfono/email opcionales; DNI/NIT único; sin tests aún)
  11. Proveedores ← hecho (2026-10-08; mismo patrón que clientes, módulo aparte)
  12. Compras (entrada de inventario: Compra + CompraDetalle con estados BORRADOR/CONFIRMADA; al confirmar ajusta stock + valorCompra/costo y recalcula recetas; sin tests aún) ← hecho (2026-10-08)
- Tras cada cambio: compilar (`./mvnw -q compile`) antes de dar por terminada la tarea.
- **Tests módulo a módulo, junto con el módulo:** cada módulo nuevo sumo sus tests con la misma convención (ver sección Tests). Hoy cubierto: `util/ValidacionDatos` completo (35 tests) + `UsuarioService` completo (22 tests, Mockito) + `IngredienteService` completo (31 tests, Mockito, 100% líneas) + `ProductoService` completo (33 tests, Mockito, 100% líneas).

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
├── model/          # @Entity (Usuario, Ingrediente, Producto, Receta, DetalleReceta, ProductoConReceta, Compra, CompraDetalle, ...) + enums (Categoria, UnidadMedida, EstadoCompra, TipoItem)
├── repository/     # interfaces JpaRepository
├── service/        # @Service, lógica de negocio
├── controller/     # @Controller MVC + Thymeleaf
├── dto/            # DTOs (Usuario con Request/Response; resto con DTO único) — Lombok
├── mapper/         # interfaces MapStruct (@Mapper(componentModel = "spring"))
├── config/         # SecurityConfig, DataInitializer (orquestador: usuarios + dispara seeders), DataSeeder (interface) + productosdemo/ (Ingrediente/Receta/ProductoConReceta/ProductoSimple/Cliente/Proveedor Seeder, @Order 1-6), etc.
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
│   │   ├── clientes/        # clientes.html (listado), cliente-form.html (alta/edición)
│   │   ├── proveedores/     # proveedores.html (listado), proveedor-form.html (alta/edición)
│   │   ├── compras/         # compras.html (listado), compra-form.html (cabecera + líneas + confirmar)
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
- **DTOs con MapStruct:** controladores y services solo ven clases de `dto/` (reciben y devuelven DTO); la entidad `Usuario` no sale de `service`/`repository`. Los mappers viven en `mapper/` como interfaz `@Mapper(componentModel = "spring")` (Spring usa la generada `*MapperImpl`). El password solo aparece en `*DTORequest`: **nunca** en un `*DTOResponse` ni en logs. Si un mapeo necesita ignorar un campo, usar `@Mapping(target = "...", ignore = true)`. **Regla anti-`merge` fantasma:** en todo `toEntity` de un ALTA cuyo POST lleva `{id}` en la path (p. ej. `POST /admin/recetas/{id}/detalles`), ignorar siempre el `id` (`@Mapping(target = "id", ignore = true)`): Spring binda las URI template variables dentro del `@ModelAttribute` y el id del path caería en `dto.id`, con lo que `save()` haría `merge()` sobre una fila inexistente → `StaleObjectStateException` (500). Detalle completo en `leccion_detalle_receta_2026-10-06.txt`.
- **DTO único (regla general):** `Producto`, `Ingrediente`, `Receta`, `DetalleReceta`, `ProductoConReceta`, `Compra` y `CompraDetalle` usan un solo `*DTO` para request y response (sin sufijos) porque no tienen campos sensibles como el password; solo `Usuario` mantiene Request/Response separados. Los mappers de DTO único exponen `toDTO/toDTOList/toEntity` (sin `toRequest`).
- Validación de entradas: **dos capas**.
  1. **Controlador** → `@Valid` + `BindingResult` sobre los DTOs (`*DTORequest`, `*DTO`) con anotaciones `jakarta.validation` (`spring-boot-starter-validation` en el pom). Si `resultado.hasErrors()` se re-renderiza la vista con `th:errors` / `th:errorclass="is-invalid"` (sin redirect).
  2. **Service** → siempre con `util/ValidacionDatos.*` (métodos static que lanzan `IllegalArgumentException`); los services no escriben `if (x == null || x.isBlank())` a mano. Las reglas que consultan la BD (p. ej. username duplicado) sí se quedan en el service. Numéricos (`stock`, `valorCompra`, `precioVenta`, `porcionAdicional/precioAdicional`) con `ValidacionDatos.stock(...)` (no nulo + `>= 0`); la regla `precioVenta >= costo` vive en `ProductoService.validarMargen(...)`.
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
  7. **Métodos `private` del service: NO se les hacen tests directos** (JUnit solo invoca públicos; llamarlos por reflección es mala práctica). Se cubren **de forma indirecta** a través de los públicos que los usan: hay que diseñar los tests de esos públicos para que **todas las ramas** del privado se ejerciten (feliz, null, excepción). JaCoCo mide líneas ejecutadas sin importar desde dónde se llamen, así que un privado queda al 100% si sus públicos tienen los tests adecuados. Ejemplo: `UsuarioService.obtenerEntidad` (ramas OK/no-encontrado) y `puedeDesactivarse` (ramas ADMIN+count / no-ADMIN) quedan cubiertos por los tests de `obtenerPorId`, `actualizar_desactivarUnicoAdmin_*` y `alternarActivo_cajero*`.
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
- `@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")` en `ClienteAdminController` y `ProveedorAdminController` (clase) y en **todos** los métodos de `ClienteService` y `ProveedorService`.
- `@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")` en `CompraAdminController` (clase) y en **todos** los métodos de `CompraService` (incluidos `ingresarStock` en `IngredienteService`/`ProductoService` y `recalcularCostosPorIngrediente` en `RecetaService`, que `CompraService.confirmar` llama con la sesión del admin).
- `/admin/recetas` y `/admin/recetas/**` → `ROLE_DEV` o `ROLE_ADMIN` (regla **antes** de `/admin/**`)
- `/admin/productos-con-receta` y `/admin/productos-con-receta/**` → `ROLE_DEV` o `ROLE_ADMIN` (regla **antes** de `/admin/**`)
- `/admin/clientes` y `/admin/clientes/**` → `ROLE_DEV` o `ROLE_ADMIN` (regla **antes** de `/admin/**`)
- `/admin/proveedores` y `/admin/proveedores/**` → `ROLE_DEV` o `ROLE_ADMIN` (regla **antes** de `/admin/**`)
- `/admin/compras` y `/admin/compras/**` → `ROLE_DEV` o `ROLE_ADMIN` (regla **antes** de `/admin/**`)
- Semillas en `config/productosdemo/` (se crean solo si no existen; **modular**: `DataInitializer` solo siembra usuarios y luego hace `seeders.forEach(DataSeeder::sembrar)` sobre el `List<DataSeeder>` inyectado — interface `DataSeeder` en `config/`, seeders en `config/productosdemo/` —, ordenado por `@Order`): `admin/admin123`, `cajero/cajero123`, `dev/dev123` + `IngredienteSeeder` `@Order(1)` (25 ingredientes de comida rápida, precios COP; los 8 marcados `esAdicional=true` llevan además `porcionAdicional`/`precioAdicional` de ejemplo: Mayonesa 0.02 L/800, Ketchup 0.02 L/800, Mostaza 0.01 L/800, Tocineta 0.03 KG/2500, Maíz dulce 0.04 KG/1500, Salsa de ajo 0.01 L/1000, Salsa picante 0.01 L/1000, Jalapeño 0.02 KG/1500; el resto 0/0) + `RecetaSeeder` `@Order(2)` (15 recetas con sus líneas, costo auto) + `ProductoConRecetaSeeder` `@Order(3)` (12 productos con receta, nombre = receta, precio >= costo, sync `enUso`; 3 recetas quedan libres: Ensalada fresca, Arepa con pollo, Pechuga a la plancha con ensalada) + `ProductoSimpleSeeder` `@Order(4)` (10 productos simples: 7 bebidas, 1 cerveza, 1 postre, 1 extra; stock/costo/precio en COP) + `ClienteSeeder` `@Order(5)` y `ProveedorSeeder` `@Order(6)` (10 contactos falsos cada uno, mezcla de personas con apellido y empresas jurídicas sin apellido; idempotentes por `existsByDniNitIgnoreCase`; `ProveedorSeeder` además siembra el **"Proveedor por defecto"** `dniNit=000000000-0` que usa el módulo de compras cuando no se escoge proveedor — y `CompraService` lo crea por repositorio como respaldo si faltara). Los seeders siembran por **repositorio directo** (nunca por service: los `@PreAuthorize` bloquearían el arranque sin sesión); cada uno es `@Component implements DataSeeder`, idempotente, y omite con `log.warn` si falta algo (receta inexistente/en uso/precio < costo). Semilla futura = nueva clase `@Order(n)`, sin tocar `DataInitializer`.
- Regla de negocio: nunca se borran usuarios (baja = `activo=false`) y no se puede desactivar al único ADMIN activo.
- Reglas de ingrediente: nunca se borran (baja = `activo=false`), `nombre` único (insensible a mayúsculas), `valorCompra` nunca nulo ni negativo (alimenta el costo de las recetas). **Sin `valorVenta` (decisión 2026-10-08):** el precio de venta del ingrediente como extra es `precioAdicional` (según `porcionAdicional`); se eliminó el campo y la regla `valorVenta >= valorCompra` (con su `IngredienteService.validarMargen`). `esAdicional` por defecto `true` (checkbox en el form + badge en el listado). `porcionAdicional` (12,3) y `precioAdicional` (12,2) nunca nulos ni negativos: **el admin los escribe a mano** en el form (decisión 2026-10-07: precio manual, NO se calcula); se muestran en el listado solo si `esAdicional` (con la unidad del ingrediente al lado de la porción). Si la tabla ya tenía filas, tras añadir las columnas rellenar con `UPDATE ingrediente SET valor_compra=0, es_adicional=true WHERE ... IS NULL;` + `UPDATE ingrediente SET porcion_adicional=0, precio_adicional=0 WHERE porcion_adicional IS NULL OR precio_adicional IS NULL;`. **Migración 2026-10-08:** `ALTER TABLE ingrediente DROP COLUMN valor_venta;`. **Entradas de stock y precio por compras (módulo 12):** `IngredienteService.ingresarStock(id, cantidad, valorCompra)` suma `stockActual += cantidad` y fija `valorCompra` = precio del lote (valida cantidad > 0 e ingrediente activo); lo llama `CompraService.confirmar`.
- Reglas de producto: nunca se borran (baja = `activo=false`), `nombre` único global (insensible a mayúsculas), `categoria` como enum `Categoria` (sin CRUD ni tabla; valores `BEBIDAS, CERVEZAS, LICORES, COMIDA, POSTRES, EXTRAS`), `stockActual/stockMinimo` (`BigDecimal` 12,3, nunca nulos ni negativos) y `costo/precioVenta` nunca nulos ni negativos con `precioVenta >= costo`. Son los productos simples (sin receta; los compuestos viven en `producto_con_receta`). Si la tabla ya tenía filas antes de añadir columnas, rellenar con `UPDATE producto SET stock_actual=0, stock_minimo=0 WHERE stock_actual IS NULL OR stock_minimo IS NULL` (igual que ingredientes). **Entradas por compras (módulo 12):** `ProductoService.ingresarStock(id, cantidad, costo)` suma `stockActual += cantidad` y fija `costo` = precio del lote; `precioVenta` NO se toca (sigue manual).
- Reglas de receta: nunca se borran (baja = `activo=false`), `nombre` único (insensible a mayúsculas), `costo` automático = suma `cantidad * ingrediente.valorCompra` (se recalcula al agregar/quitar líneas, nunca se edita a mano). `DetalleReceta` con `ingrediente+cantidad>0`, unique `(receta,ingrediente)`, ingrediente debe estar activo. **La `unidadMedida` del detalle NO se elige en el form: la fija `RecetaService.agregarDetalle` desde `ingrediente.unidadMedida`** (lo que mande el cliente se ignora; el dropdown del form la muestra entre paréntesis "Nombre (UD)" sin JS, y `DetalleRecetaDTO.unidadMedida` va sin `@NotNull`). Decisión 2026-09-30: sin snapshot de valor en el detalle (costo en vivo desde `valorCompra` actual). Decisión 2026-10-06: sin conversión de unidades (si el ingrediente es KG, la receta usa KG; G/ML quedan fuera de alcance). **Recálculo por compras (módulo 12):** `RecetaService.recalcularCostosPorIngrediente(ingredienteId)` recalcula todas las recetas que usan ese ingrediente (lo llama `CompraService.confirmar` después de fijar el nuevo `valorCompra`).
- Reglas de producto con receta: nunca se borran, `nombre` único, relación 1-a-1 con `Receta` (una receta solo en un producto), `precioVenta >= receta.costo` (el admin fija el precio viendo el costo). **Adicionales (decisión 2026-10-08, Propuesta 1): NO hay flag ni lista por producto** — se eliminó el boolean `admiteAdicionales` (entity/DTO/form/listado); en la venta futura los extras salen de los ingredientes con `esAdicional=true` (lista global, sin configuración por producto). Lo que el cajero marca es **transitorio de la comanda**: vive solo en el módulo de venta (DTO de línea con la lista de extras inicializada `new ArrayList<>()` — vacía, nunca null —, se suma `precioAdicional` al cobrar y la comanda queda vacía al cerrar la venta). Flag `Receta.enUso` (boolean NN, defecto `false`): lo sincroniza `ProductoConRecetaService` (crear → `true`; actualizar que cambia de receta → vieja `false` + nueva `true`; baja de producto NO lo libera — decisión 2026-10-07). El select del form muestra solo recetas activas y `!enUso` (más la propia en edición) vía helper `recetasDisponibles`; el guard autoritativo sigue siendo `existsByRecetaId` + unique en la BD. Badge "En uso/Libre" en el listado de recetas. Si la tabla `receta` ya tenía filas: `UPDATE receta SET en_uso=false WHERE en_uso IS NULL;` + `UPDATE receta r JOIN producto_con_receta p ON p.receta_id=r.id SET r.en_uso=true;`. **Migración 2026-10-08** (Hibernate no borra columnas con `ddl-auto=update`): `ALTER TABLE producto_con_receta DROP COLUMN admite_adicionales;`
- Reglas de cliente y proveedor (módulos 10-11, 2026-10-08): dos módulos separados con el mismo patrón CRUD admin (DTO único + MapStruct, sin relaciones). Campos: `nombre` (nombre o nombre jurídico) y `dniNit` (DNI o NIT, columna `dni_nit` 15) **obligatorios**; `apellido`, `direccion`, `telefono`, `email` opcionales (`email` con `@Email` en el DTO, capa 1). `dniNit` **único** por tabla (insensible a mayúsculas) → "Ya existe un cliente/proveedor con ese DNI/NIT" (los nombres SÍ pueden repetirse). Nunca se borran (baja = `activo=false`). Con semillas: `ClienteSeeder` `@Order(5)` y `ProveedorSeeder` `@Order(6)` (10 + 10 contactos falsos + **"Proveedor por defecto"** `000000000-0` en proveedores; idempotentes por dniNit). Sin tests aún (pendiente de la próxima jornada, con el resto de services).
- Reglas de compra (módulo 12, 2026-10-08): `Compra` nace en estado `BORRADOR` (enum `EstadoCompra`: BORRADOR/CONFIRMADA) con `total=0`; las líneas se agregan/quitan sin tocar inventario y el botón **Confirmar** ajusta todo de una vez (todo en una transacción; confirmada ya no se edita ni se reconfirma). `CompraDetalle` con `tipo` (enum `TipoItem`: INGREDIENTE/PRODUCTO) + `ingrediente`/`producto` nullable + `cantidad>0` + `precioUnitario>=0` + `subtotal` auto (`cantidad*precio`); `total` de la compra = suma de subtotales (se recalcula al agregar/quitar líneas). Sin duplicados: un ítem solo una vez por compra (unique `(compra_id,ingrediente_id)` + `(compra_id,producto_id)` — con NULLs MySQL permite el otro tipo — más `existsBy*` en el service). Ítem debe estar **activo** en ambos casos; solo productos simples (tabla `producto`). Proveedor **obligatorio**: el form lleva opción "Sin proveedor (por defecto)" y `CompraService` resuelve/crea el "Proveedor por defecto" (ver semillas). Al confirmar: ingrediente → `stockActual += cantidad` + `valorCompra` = precio del lote y `recalcularCostosPorIngrediente`; producto → `stockActual += cantidad` + `costo` = precio (**`precioVenta`/`precioAdicional` nunca se tocan**). Nunca se borran compras (sin `activo`, sin endpoint de baja). Vistas sin JS: `compra-form.html` con dos mini-forms ("Agregar ingrediente" → `POST /{id}/detalles`, "Agregar producto" → `POST /{id}/detalles-producto`; el controlador fuerza `tipo`); `CompraDTO.fecha` es String ya formateado `dd/MM/yyyy HH:mm` por expresión MapStruct (**sin** `thymeleaf-extras-java8time` en el pom). Sin tests aún (pendiente de la próxima jornada, con el resto de services).

## Notas y riesgos
- **Tests:** hay 122 (35 de `TestValidacionDatos` sin BD + 22 de `TestUsuarioService` con Mockito sin BD + 31 de `TestIngredienteService` con Mockito sin BD + 33 de `TestProductoService` con Mockito sin BD + 1 `EmpresaApplicationTests` con `@SpringBootTest` que conecta a MySQL). La verificación diaria es `./mvnw -q compile` + `./mvnw test`. `./mvnw verify` aplica la regla de cobertura del 90% (ya fiable, ver sección "Tests y cobertura") y hoy falla: `UsuarioService`, `IngredienteService` e `ProductoService` están cubiertos pero los services restantes (`Receta` —creció con `recalcularCostosPorIngrediente`—, `ProductoConReceta`, `Cliente`, `Proveedor`, `Compra`) y todos los controllers todavía no tienen tests. **Pendiente (próxima jornada de tests):** `Test*Service` con Mockito + `DataProvider*` + convención `Test*`/Given-When-Then (siguiente: `TestRecetaService`, luego `ProductoConReceta`, `Cliente`, `Proveedor`, `Compra`).
- Si falta config de datasource o la BD no existe, `spring-boot:run` fallará.
- Tras el login, `/admin` y `/venta` pueden devolver 404 hasta que se implementen esos módulos: es esperado.
- El `pom.xml` tiene elementos vacíos heredados (`<licenses>`, `<developers>`, `<scm>`) — no rellenar salvo que se pida.
- Documentación de arranque de Spring en `HELP.md`.
- Próximo trabajo (inventario + config global + validador POS): `pendiente_2026-09-29.txt` (nada implementado; decisiones: sin PIN, `controlaInventario` boolean solo-ADMIN en Producto e Ingrediente, caché en memoria pendiente de explicar).
- Git + GitHub activos desde 2026-09-30: repo `josegregoppdev/mibombayp`, rama `main`, trabajo por ramas/commits. No se suben `*.txt` ni `application.properties` (ver `.gitignore`); la plantilla es `application.properties.example`.
