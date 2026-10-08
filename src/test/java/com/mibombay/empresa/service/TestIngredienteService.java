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

import com.mibombay.empresa.dto.IngredienteDTO;
import com.mibombay.empresa.mapper.IngredienteMapper;
import com.mibombay.empresa.model.Ingrediente;
import com.mibombay.empresa.model.UnidadMedida;
import com.mibombay.empresa.repository.IngredienteRepository;

@ExtendWith(MockitoExtension.class)
class TestIngredienteService {

	@Mock
	private IngredienteRepository ingredienteRepository;

	@Mock
	private IngredienteMapper ingredienteMapper;

	@InjectMocks
	private IngredienteService ingredienteService;

	// ------------------------------------------------------------------
	// listar
	// ------------------------------------------------------------------

	// Camino feliz: 3 ingredientes en BD -> devuelve 3 DTOs mapeados
	@Test
	void listar_variosIngredientes_devuelveListaMapeada() {
		// Given
		List<Ingrediente> entidades = DataProviderIngrediente.listaIngredientes();
		List<IngredienteDTO> dtos = List.of(dtoDe(1L, "Pan perro"), dtoDe(2L, "Salchicha"), dtoDe(3L, "Mayonesa"));
		when(ingredienteRepository.findAll(Sort.by("nombre"))).thenReturn(entidades);
		when(ingredienteMapper.toDTOList(entidades)).thenReturn(dtos);

		// When
		List<IngredienteDTO> resultado = ingredienteService.listar();

		// Then
		assertEquals(3, resultado.size());
		assertEquals("Pan perro", resultado.get(0).getNombre());
		verify(ingredienteRepository).findAll(Sort.by("nombre"));
		verify(ingredienteMapper).toDTOList(entidades);
	}

	// Camino vacío: sin ingredientes en BD -> devuelve lista vacía
	@Test
	void listar_sinIngredientes_devuelveListaVacia() {
		// Given
		List<Ingrediente> entidades = List.of();
		when(ingredienteRepository.findAll(Sort.by("nombre"))).thenReturn(entidades);
		when(ingredienteMapper.toDTOList(anyList())).thenReturn(List.of());

		// When
		List<IngredienteDTO> resultado = ingredienteService.listar();

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
		Ingrediente entidad = DataProviderIngrediente.ingredienteValido();
		IngredienteDTO esperado = dtoDe(1L, "Pan perro");
		when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(ingredienteMapper.toDTO(entidad)).thenReturn(esperado);

		// When
		IngredienteDTO resultado = ingredienteService.obtenerPorId(1L);

		// Then
		assertEquals("Pan perro", resultado.getNombre());
		verify(ingredienteRepository).findById(1L);
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void obtenerPorId_idInexistente_lanzaNoSuchElement() {
		// Given
		when(ingredienteRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class, () -> ingredienteService.obtenerPorId(99L));
	}

	// ------------------------------------------------------------------
	// crear
	// ------------------------------------------------------------------

	// Camino feliz: DTO válido -> valida, guarda activo y devuelve el DTO
	@Test
	void crear_dtoValido_guardaConActivoTrue() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		Ingrediente entidad = DataProviderIngrediente.ingredienteValido();
		IngredienteDTO respuesta = dtoDe(10L, "Pan perro");
		when(ingredienteRepository.existsByNombreIgnoreCase("Pan perro")).thenReturn(false);
		when(ingredienteMapper.toEntity(dto)).thenReturn(entidad);
		when(ingredienteRepository.save(entidad)).thenReturn(entidad);
		when(ingredienteMapper.toDTO(entidad)).thenReturn(respuesta);

		// When
		IngredienteDTO resultado = ingredienteService.crear(dto);

		// Then
		assertEquals(10L, resultado.getId());
		ArgumentCaptor<Ingrediente> captor = ArgumentCaptor.forClass(Ingrediente.class);
		verify(ingredienteRepository).save(captor.capture());
		Ingrediente guardado = captor.getValue();
		assertTrue(guardado.isActivo());
		assertEquals("Pan perro", guardado.getNombre());
		assertEquals(UnidadMedida.UD, guardado.getUnidadMedida());
		assertTrue(guardado.isEsAdicional());
	}

	// Excepción: nombre vacío -> ValidacionDatos lanza antes de tocar la BD
	@Test
	void crear_nombreVacio_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setNombre("   ");

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.crear(dto));
		assertEquals("El nombre: no puede estar vacío", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: nombre con inyección -> ValidacionDatos lanza antes de tocar la BD
	@Test
	void crear_nombreConInyeccion_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setNombre("Pan; DROP TABLE ingrediente");

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.crear(dto));
		assertEquals("El nombre: contiene caracteres no permitidos", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: nombre de más de 100 caracteres -> ValidacionDatos lanza
	@Test
	void crear_nombreLargo_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setNombre("a".repeat(101));

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.crear(dto));
		assertEquals("El nombre: máximo 100 caracteres", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: unidad de medida nula -> ValidacionDatos.requerido lanza
	@Test
	void crear_unidadNula_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setUnidadMedida(null);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.crear(dto));
		assertEquals("La unidad de medida: no puede estar vacío", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: stock actual nulo -> ValidacionDatos.stock lanza
	@Test
	void crear_stockActualNulo_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setStockActual(null);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.crear(dto));
		assertEquals("El stock actual: no puede estar vacío", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: stock mínimo negativo -> ValidacionDatos.stock lanza
	@Test
	void crear_stockMinimoNegativo_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setStockMinimo(new BigDecimal("-1"));

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.crear(dto));
		assertEquals("El stock mínimo: no puede ser negativo", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: valor de compra nulo -> ValidacionDatos.stock lanza
	@Test
	void crear_valorCompraNulo_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setValorCompra(null);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.crear(dto));
		assertEquals("El valor de compra: no puede estar vacío", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: porción adicional negativa -> ValidacionDatos.stock lanza
	@Test
	void crear_porcionAdicionalNegativa_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setPorcionAdicional(new BigDecimal("-0.01"));

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.crear(dto));
		assertEquals("La porción adicional: no puede ser negativo", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: precio adicional nulo -> ValidacionDatos.stock lanza
	@Test
	void crear_precioAdicionalNulo_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setPrecioAdicional(null);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.crear(dto));
		assertEquals("El precio adicional: no puede estar vacío", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: nombre ya registrado -> rechaza sin guardar ni mapear
	@Test
	void crear_nombreDuplicado_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		when(ingredienteRepository.existsByNombreIgnoreCase("Pan perro")).thenReturn(true);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.crear(dto));
		assertEquals("Ya existe un ingrediente con ese nombre", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
		verify(ingredienteMapper, never()).toEntity(any(IngredienteDTO.class));
	}

	// ------------------------------------------------------------------
	// actualizar
	// ------------------------------------------------------------------

	// Camino feliz: DTO válido -> aplica cambios (incluido activo) y guarda
	@Test
	void actualizar_dtoValido_guardaCambios() {
		// Given
		Ingrediente entidad = DataProviderIngrediente.ingredienteValido();
		IngredienteDTO dto = DataProviderIngrediente.dtoDesactivado();
		dto.setNombre("Pan perro doble");
		IngredienteDTO respuesta = dtoDe(1L, "Pan perro doble");
		when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(ingredienteRepository.existsByNombreIgnoreCase("Pan perro doble")).thenReturn(false);
		when(ingredienteRepository.save(entidad)).thenReturn(entidad);
		when(ingredienteMapper.toDTO(entidad)).thenReturn(respuesta);

		// When
		IngredienteDTO resultado = ingredienteService.actualizar(1L, dto);

		// Then
		assertEquals(1L, resultado.getId());
		ArgumentCaptor<Ingrediente> captor = ArgumentCaptor.forClass(Ingrediente.class);
		verify(ingredienteRepository).save(captor.capture());
		Ingrediente guardado = captor.getValue();
		assertEquals("Pan perro doble", guardado.getNombre());
		assertFalse(guardado.isActivo());
	}

	// Excepción: nombre duplicado en otro ingrediente -> rechaza sin guardar
	@Test
	void actualizar_nombreDuplicadoEnOtro_lanzaExcepcion() {
		// Given
		Ingrediente entidad = DataProviderIngrediente.ingredienteValido();
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setNombre("Salchicha");
		when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(ingredienteRepository.existsByNombreIgnoreCase("Salchicha")).thenReturn(true);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.actualizar(1L, dto));
		assertEquals("Ya existe un ingrediente con ese nombre", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Camino feliz: mismo nombre -> no consulta duplicados (corto por equalsIgnoreCase)
	@Test
	void actualizar_mismoNombreNoConsultaDuplicado() {
		// Given
		Ingrediente entidad = DataProviderIngrediente.ingredienteValido();
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setNombre("PAN PERRO");
		when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(ingredienteRepository.save(entidad)).thenReturn(entidad);
		when(ingredienteMapper.toDTO(entidad)).thenReturn(dtoDe(1L, "PAN PERRO"));

		// When
		ingredienteService.actualizar(1L, dto);

		// Then
		verify(ingredienteRepository, never()).existsByNombreIgnoreCase(any());
		assertEquals("PAN PERRO", entidad.getNombre());
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void actualizar_idInexistente_lanzaNoSuchElement() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		when(ingredienteRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class, () -> ingredienteService.actualizar(99L, dto));
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: nombre vacío -> validación antes de buscar en BD
	@Test
	void actualizar_nombreVacio_lanzaExcepcion() {
		// Given
		IngredienteDTO dto = DataProviderIngrediente.dtoValido();
		dto.setNombre("");

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> ingredienteService.actualizar(1L, dto));
		verify(ingredienteRepository, never()).findById(anyLong());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// ------------------------------------------------------------------
	// alternarActivo
	// ------------------------------------------------------------------

	// Camino feliz: inactivo -> se activa
	@Test
	void alternarActivo_ingredienteInactivo_loActiva() {
		// Given
		Ingrediente ingrediente = DataProviderIngrediente.ingredienteInactivo();
		when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ingrediente));
		when(ingredienteRepository.save(ingrediente)).thenReturn(ingrediente);
		when(ingredienteMapper.toDTO(ingrediente)).thenReturn(dtoDe(1L, "Pan perro"));

		// When
		ingredienteService.alternarActivo(1L);

		// Then
		assertTrue(ingrediente.isActivo());
		verify(ingredienteRepository).save(ingrediente);
	}

	// Camino feliz: activo -> se desactiva
	@Test
	void alternarActivo_ingredienteActivo_loDesactiva() {
		// Given
		Ingrediente ingrediente = DataProviderIngrediente.ingredienteValido();
		when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ingrediente));
		when(ingredienteRepository.save(ingrediente)).thenReturn(ingrediente);
		when(ingredienteMapper.toDTO(ingrediente)).thenReturn(dtoDe(1L, "Pan perro"));

		// When
		ingredienteService.alternarActivo(1L);

		// Then
		assertFalse(ingrediente.isActivo());
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void alternarActivo_idInexistente_lanzaNoSuchElement() {
		// Given
		when(ingredienteRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class, () -> ingredienteService.alternarActivo(99L));
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// ------------------------------------------------------------------
	// ingresarStock
	// ------------------------------------------------------------------

	// Camino feliz: suma el stock y fija valorCompra al precio del lote
	@Test
	void ingresarStock_valido_sumaStockYFijaValorCompra() {
		// Given
		Ingrediente ingrediente = DataProviderIngrediente.ingredienteValido();
		when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(ingrediente));
		when(ingredienteRepository.save(ingrediente)).thenReturn(ingrediente);

		// When
		ingredienteService.ingresarStock(1L, new BigDecimal("10"), new BigDecimal("900"));

		// Then
		ArgumentCaptor<Ingrediente> captor = ArgumentCaptor.forClass(Ingrediente.class);
		verify(ingredienteRepository).save(captor.capture());
		Ingrediente guardado = captor.getValue();
		assertEquals(new BigDecimal("110"), guardado.getStockActual());
		assertEquals(new BigDecimal("900"), guardado.getValorCompra());
	}

	// Excepción: id nulo -> ValidacionDatos.requerido lanza antes de la BD
	@Test
	void ingresarStock_idNulo_lanzaExcepcion() {
		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.ingresarStock(null, new BigDecimal("10"), new BigDecimal("900")));
		assertEquals("El ingrediente: no puede estar vacío", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: cantidad nula -> ValidacionDatos.stock lanza
	@Test
	void ingresarStock_cantidadNula_lanzaExcepcion() {
		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.ingresarStock(1L, null, new BigDecimal("900")));
		assertEquals("La cantidad: no puede estar vacío", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: cantidad en cero -> debe ser mayor a cero
	@Test
	void ingresarStock_cantidadCero_lanzaExcepcion() {
		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.ingresarStock(1L, BigDecimal.ZERO, new BigDecimal("900")));
		assertEquals("La cantidad: debe ser mayor a cero", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: cantidad negativa -> ValidacionDatos.stock lanza antes del chequeo > 0
	@Test
	void ingresarStock_cantidadNegativa_lanzaExcepcion() {
		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.ingresarStock(1L, new BigDecimal("-5"), new BigDecimal("900")));
		assertEquals("La cantidad: no puede ser negativo", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: valor de compra nulo -> ValidacionDatos.stock lanza
	@Test
	void ingresarStock_valorCompraNulo_lanzaExcepcion() {
		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.ingresarStock(1L, new BigDecimal("10"), null));
		assertEquals("El valor de compra: no puede estar vacío", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: ingrediente inactivo -> rechaza el ingreso sin guardar
	@Test
	void ingresarStock_ingredienteInactivo_lanzaExcepcion() {
		// Given
		Ingrediente inactivo = DataProviderIngrediente.ingredienteInactivo();
		when(ingredienteRepository.findById(1L)).thenReturn(Optional.of(inactivo));

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> ingredienteService.ingresarStock(1L, new BigDecimal("10"), new BigDecimal("900")));
		assertEquals("El ingrediente está inactivo", error.getMessage());
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void ingresarStock_idInexistente_lanzaNoSuchElement() {
		// Given
		when(ingredienteRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class,
				() -> ingredienteService.ingresarStock(99L, new BigDecimal("10"), new BigDecimal("900")));
		verify(ingredienteRepository, never()).save(any(Ingrediente.class));
	}

	// ------------------------------------------------------------------
	// helpers
	// ------------------------------------------------------------------

	private IngredienteDTO dtoDe(Long id, String nombre) {
		IngredienteDTO dto = new IngredienteDTO();
		dto.setId(id);
		dto.setNombre(nombre);
		dto.setUnidadMedida(UnidadMedida.UD);
		dto.setActivo(true);
		return dto;
	}

}
