package com.mibombay.empresa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.mibombay.empresa.dto.ProductoDTO;
import com.mibombay.empresa.mapper.ProductoMapper;
import com.mibombay.empresa.model.Categoria;
import com.mibombay.empresa.model.Producto;
import com.mibombay.empresa.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class TestProductoService {

	@Mock
	private ProductoRepository productoRepository;

	@Mock
	private ProductoMapper productoMapper;

	@InjectMocks
	private ProductoService productoService;

	// ------------------------------------------------------------------
	// listar
	// ------------------------------------------------------------------

	// Camino feliz: 3 productos en BD -> devuelve 3 DTOs mapeados
	@Test
	void listar_variosProductos_devuelveListaMapeada() {
		// Given
		List<Producto> entidades = DataProviderProducto.listaProductos();
		List<ProductoDTO> dtos = List.of(dtoDe(1L, "Coca-Cola 400 ml"), dtoDe(2L, "Pilsen 330 ml"),
				dtoDe(3L, "Brownie"));
		when(productoRepository.findAll(Sort.by("nombre"))).thenReturn(entidades);
		when(productoMapper.toDTOList(entidades)).thenReturn(dtos);

		// When
		List<ProductoDTO> resultado = productoService.listar();

		// Then
		assertEquals(3, resultado.size());
		assertEquals("Coca-Cola 400 ml", resultado.get(0).getNombre());
		verify(productoRepository).findAll(Sort.by("nombre"));
		verify(productoMapper).toDTOList(entidades);
	}

	// Camino vacío: sin productos en BD -> devuelve lista vacía
	@Test
	void listar_sinProductos_devuelveListaVacia() {
		// Given
		List<Producto> entidades = List.of();
		when(productoRepository.findAll(Sort.by("nombre"))).thenReturn(entidades);
		when(productoMapper.toDTOList(anyList())).thenReturn(List.of());

		// When
		List<ProductoDTO> resultado = productoService.listar();

		// Then
		assertTrue(resultado.isEmpty());
	}

	// ------------------------------------------------------------------
	// obtenerPorId
	// ------------------------------------------------------------------

	// Camino feliz: id existe -> devuelve el DTO mapeado
	@Test
	void obtenerPorId_idExistente_devuelveDTO() {
		// Given
		Producto entidad = DataProviderProducto.productoValido();
		ProductoDTO esperado = dtoDe(1L, "Coca-Cola 400 ml");
		when(productoRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(productoMapper.toDTO(entidad)).thenReturn(esperado);

		// When
		ProductoDTO resultado = productoService.obtenerPorId(1L);

		// Then
		assertEquals("Coca-Cola 400 ml", resultado.getNombre());
		verify(productoRepository).findById(1L);
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void obtenerPorId_idInexistente_lanzaNoSuchElement() {
		// Given
		when(productoRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class, () -> productoService.obtenerPorId(99L));
	}

	// ------------------------------------------------------------------
	// crear
	// ------------------------------------------------------------------

	// Camino feliz: DTO válido -> valida, guarda activo y devuelve el DTO
	@Test
	void crear_dtoValido_guardaConActivoTrue() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		Producto entidad = DataProviderProducto.productoValido();
		ProductoDTO respuesta = dtoDe(10L, "Coca-Cola 400 ml");
		when(productoRepository.existsByNombreIgnoreCase("Coca-Cola 400 ml")).thenReturn(false);
		when(productoMapper.toEntity(dto)).thenReturn(entidad);
		when(productoRepository.save(entidad)).thenReturn(entidad);
		when(productoMapper.toDTO(entidad)).thenReturn(respuesta);

		// When
		ProductoDTO resultado = productoService.crear(dto);

		// Then
		assertEquals(10L, resultado.getId());
		ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
		verify(productoRepository).save(captor.capture());
		Producto guardado = captor.getValue();
		assertTrue(guardado.isActivo());
		assertEquals("Coca-Cola 400 ml", guardado.getNombre());
		assertEquals(Categoria.BEBIDAS, guardado.getCategoria());
		assertEquals(new BigDecimal("1800"), guardado.getCosto());
		assertEquals(new BigDecimal("2000"), guardado.getPrecioVenta());
	}

	// Excepción: nombre vacío -> ValidacionDatos lanza antes de tocar la BD
	@Test
	void crear_nombreVacio_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setNombre("  ");

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.crear(dto));
		assertEquals("El nombre: no puede estar vacío", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: nombre con inyección -> ValidacionDatos lanza antes de tocar la BD
	@Test
	void crear_nombreConInyeccion_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setNombre("Coca; DROP TABLE producto");

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.crear(dto));
		assertEquals("El nombre: contiene caracteres no permitidos", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: nombre de más de 100 caracteres -> ValidacionDatos lanza
	@Test
	void crear_nombreLargo_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setNombre("a".repeat(101));

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.crear(dto));
		assertEquals("El nombre: máximo 100 caracteres", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: categoría nula -> ValidacionDatos.requerido lanza
	@Test
	void crear_categoriaNula_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setCategoria(null);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.crear(dto));
		assertEquals("La categoría: no puede estar vacío", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: stock actual nulo -> ValidacionDatos.stock lanza
	@Test
	void crear_stockActualNulo_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setStockActual(null);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.crear(dto));
		assertEquals("El stock actual: no puede estar vacío", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: stock mínimo negativo -> ValidacionDatos.stock lanza
	@Test
	void crear_stockMinimoNegativo_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setStockMinimo(new BigDecimal("-1"));

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.crear(dto));
		assertEquals("El stock mínimo: no puede ser negativo", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: costo nulo -> ValidacionDatos.stock lanza
	@Test
	void crear_costoNulo_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setCosto(null);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.crear(dto));
		assertEquals("El costo: no puede estar vacío", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: precio de venta nulo -> ValidacionDatos.stock lanza
	@Test
	void crear_precioVentaNulo_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setPrecioVenta(null);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.crear(dto));
		assertEquals("El precio de venta: no puede estar vacío", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: precio de venta menor al costo -> validarMargen lanza sin guardar
	@Test
	void crear_precioVentaMenorQueCosto_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoPrecioBajoCosto();

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.crear(dto));
		assertEquals("El precio de venta no puede ser menor al costo", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Camino feliz: precio igual al costo -> el margen acepta el límite
	@Test
	void crear_precioVentaIgualACosto_guarda() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setPrecioVenta(dto.getCosto());
		Producto entidad = DataProviderProducto.productoValido();
		entidad.setPrecioVenta(new BigDecimal("1800"));
		when(productoRepository.existsByNombreIgnoreCase("Coca-Cola 400 ml")).thenReturn(false);
		when(productoMapper.toEntity(dto)).thenReturn(entidad);
		when(productoRepository.save(entidad)).thenReturn(entidad);
		when(productoMapper.toDTO(entidad)).thenReturn(dtoDe(10L, "Coca-Cola 400 ml"));

		// When
		productoService.crear(dto);

		// Then
		verify(productoRepository).save(entidad);
	}

	// Excepción: nombre ya registrado -> rechaza sin guardar ni mapear
	@Test
	void crear_nombreDuplicado_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		when(productoRepository.existsByNombreIgnoreCase("Coca-Cola 400 ml")).thenReturn(true);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.crear(dto));
		assertEquals("Ya existe un producto con ese nombre", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
		verify(productoMapper, never()).toEntity(any(ProductoDTO.class));
	}

	// ------------------------------------------------------------------
	// actualizar
	// ------------------------------------------------------------------

	// Camino feliz: DTO válido -> aplica cambios (incluido activo) y guarda
	@Test
	void actualizar_dtoValido_guardaCambios() {
		// Given
		Producto entidad = DataProviderProducto.productoValido();
		ProductoDTO dto = DataProviderProducto.dtoDesactivado();
		dto.setNombre("Coca-Cola 500 ml");
		when(productoRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(productoRepository.existsByNombreIgnoreCase("Coca-Cola 500 ml")).thenReturn(false);
		when(productoRepository.save(entidad)).thenReturn(entidad);
		when(productoMapper.toDTO(entidad)).thenReturn(dtoDe(1L, "Coca-Cola 500 ml"));

		// When
		ProductoDTO resultado = productoService.actualizar(1L, dto);

		// Then
		assertEquals(1L, resultado.getId());
		ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
		verify(productoRepository).save(captor.capture());
		Producto guardado = captor.getValue();
		assertEquals("Coca-Cola 500 ml", guardado.getNombre());
		assertFalse(guardado.isActivo());
	}

	// Excepción: nombre duplicado en otro producto -> rechaza sin guardar
	@Test
	void actualizar_nombreDuplicadoEnOtro_lanzaExcepcion() {
		// Given
		Producto entidad = DataProviderProducto.productoValido();
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setNombre("Pilsen 330 ml");
		when(productoRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(productoRepository.existsByNombreIgnoreCase("Pilsen 330 ml")).thenReturn(true);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.actualizar(1L, dto));
		assertEquals("Ya existe un producto con ese nombre", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Camino feliz: mismo nombre -> no consulta duplicados (corto por equalsIgnoreCase)
	@Test
	void actualizar_mismoNombreNoConsultaDuplicado() {
		// Given
		Producto entidad = DataProviderProducto.productoValido();
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setNombre("COCA-COLA 400 ML");
		when(productoRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(productoRepository.save(entidad)).thenReturn(entidad);
		when(productoMapper.toDTO(entidad)).thenReturn(dtoDe(1L, "COCA-COLA 400 ML"));

		// When
		productoService.actualizar(1L, dto);

		// Then
		verify(productoRepository, never()).existsByNombreIgnoreCase(any());
		assertEquals("COCA-COLA 400 ML", entidad.getNombre());
	}

	// Excepción: precio de venta menor al costo -> validarMargen antes de buscar en BD
	@Test
	void actualizar_precioVentaMenorQueCosto_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoPrecioBajoCosto();

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.actualizar(1L, dto));
		assertEquals("El precio de venta no puede ser menor al costo", error.getMessage());
		verify(productoRepository, never()).findById(anyLong());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: categoría nula -> validación antes de buscar en BD
	@Test
	void actualizar_categoriaNula_lanzaExcepcion() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		dto.setCategoria(null);

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> productoService.actualizar(1L, dto));
		verify(productoRepository, never()).findById(anyLong());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void actualizar_idInexistente_lanzaNoSuchElement() {
		// Given
		ProductoDTO dto = DataProviderProducto.dtoValido();
		when(productoRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class, () -> productoService.actualizar(99L, dto));
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// ------------------------------------------------------------------
	// alternarActivo
	// ------------------------------------------------------------------

	// Camino feliz: inactivo -> se activa
	@Test
	void alternarActivo_productoInactivo_loActiva() {
		// Given
		Producto producto = DataProviderProducto.productoInactivo();
		when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
		when(productoRepository.save(producto)).thenReturn(producto);
		when(productoMapper.toDTO(producto)).thenReturn(dtoDe(1L, "Coca-Cola 400 ml"));

		// When
		productoService.alternarActivo(1L);

		// Then
		assertTrue(producto.isActivo());
		verify(productoRepository).save(producto);
	}

	// Camino feliz: activo -> se desactiva
	@Test
	void alternarActivo_productoActivo_loDesactiva() {
		// Given
		Producto producto = DataProviderProducto.productoValido();
		when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
		when(productoRepository.save(producto)).thenReturn(producto);
		when(productoMapper.toDTO(producto)).thenReturn(dtoDe(1L, "Coca-Cola 400 ml"));

		// When
		productoService.alternarActivo(1L);

		// Then
		assertFalse(producto.isActivo());
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void alternarActivo_idInexistente_lanzaNoSuchElement() {
		// Given
		when(productoRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class, () -> productoService.alternarActivo(99L));
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// ------------------------------------------------------------------
	// ingresarStock
	// ------------------------------------------------------------------

	// Camino feliz: suma el stock y fija costo al precio del lote (precioVenta intacto)
	@Test
	void ingresarStock_valido_sumaStockYFijaCosto() {
		// Given
		Producto producto = DataProviderProducto.productoValido();
		when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
		when(productoRepository.save(producto)).thenReturn(producto);

		// When
		productoService.ingresarStock(1L, new BigDecimal("6"), new BigDecimal("1750"));

		// Then
		ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
		verify(productoRepository).save(captor.capture());
		Producto guardado = captor.getValue();
		assertEquals(new BigDecimal("54"), guardado.getStockActual());
		assertEquals(new BigDecimal("1750"), guardado.getCosto());
		assertEquals(new BigDecimal("2000"), guardado.getPrecioVenta());
	}

	// Excepción: id nulo -> ValidacionDatos.requerido lanza antes de la BD
	@Test
	void ingresarStock_idNulo_lanzaExcepcion() {
		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.ingresarStock(null, new BigDecimal("6"), new BigDecimal("1750")));
		assertEquals("El producto: no puede estar vacío", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: cantidad nula -> ValidacionDatos.stock lanza
	@Test
	void ingresarStock_cantidadNula_lanzaExcepcion() {
		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.ingresarStock(1L, null, new BigDecimal("1750")));
		assertEquals("La cantidad: no puede estar vacío", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: cantidad en cero -> debe ser mayor a cero
	@Test
	void ingresarStock_cantidadCero_lanzaExcepcion() {
		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.ingresarStock(1L, BigDecimal.ZERO, new BigDecimal("1750")));
		assertEquals("La cantidad: debe ser mayor a cero", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: cantidad negativa -> ValidacionDatos.stock lanza antes del chequeo > 0
	@Test
	void ingresarStock_cantidadNegativa_lanzaExcepcion() {
		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.ingresarStock(1L, new BigDecimal("-3"), new BigDecimal("1750")));
		assertEquals("La cantidad: no puede ser negativo", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: costo nulo -> ValidacionDatos.stock lanza
	@Test
	void ingresarStock_costoNulo_lanzaExcepcion() {
		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.ingresarStock(1L, new BigDecimal("6"), null));
		assertEquals("El costo: no puede estar vacío", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: producto inactivo -> rechaza el ingreso sin guardar
	@Test
	void ingresarStock_productoInactivo_lanzaExcepcion() {
		// Given
		Producto inactivo = DataProviderProducto.productoInactivo();
		when(productoRepository.findById(1L)).thenReturn(Optional.of(inactivo));

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> productoService.ingresarStock(1L, new BigDecimal("6"), new BigDecimal("1750")));
		assertEquals("El producto está inactivo", error.getMessage());
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void ingresarStock_idInexistente_lanzaNoSuchElement() {
		// Given
		when(productoRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class,
				() -> productoService.ingresarStock(99L, new BigDecimal("6"), new BigDecimal("1750")));
		verify(productoRepository, never()).save(any(Producto.class));
	}

	// ------------------------------------------------------------------
	// helpers
	// ------------------------------------------------------------------

	private ProductoDTO dtoDe(Long id, String nombre) {
		ProductoDTO dto = new ProductoDTO();
		dto.setId(id);
		dto.setNombre(nombre);
		dto.setCategoria(Categoria.BEBIDAS);
		dto.setActivo(true);
		return dto;
	}

}
