package com.mibombay.empresa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mibombay.empresa.dto.UsuarioDTORequest;
import com.mibombay.empresa.dto.UsuarioDTOResponse;
import com.mibombay.empresa.mapper.UsuarioMapper;
import com.mibombay.empresa.model.Rol;
import com.mibombay.empresa.model.Usuario;
import com.mibombay.empresa.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class TestUsuarioService {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private UsuarioMapper usuarioMapper;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UsuarioService usuarioService;

	// ------------------------------------------------------------------
	// listar
	// ------------------------------------------------------------------

	// Camino feliz: 3 usuarios en BD -> devuelve 3 DTOs mapeados
	@Test
	void listar_variosUsuarios_devuelveListaMapeada() {
		// Given
		List<Usuario> entidades = DataProviderUsuario.listaUsuarios();
		List<UsuarioDTOResponse> dtos = List.of(responseDe(1L, "admin"), responseDe(2L, "pepe"), responseDe(3L, "dev"));
		when(usuarioRepository.findAll(Sort.by("username"))).thenReturn(entidades);
		when(usuarioMapper.toResponseList(entidades)).thenReturn(dtos);

		// When
		List<UsuarioDTOResponse> resultado = usuarioService.listar();

		// Then
		assertEquals(3, resultado.size());
		assertEquals("admin", resultado.get(0).getUsername());
		verify(usuarioRepository).findAll(Sort.by("username"));
		verify(usuarioMapper).toResponseList(entidades);
	}

	// Camino vacío: sin usuarios en BD -> devuelve lista vacía
	@Test
	void listar_sinUsuarios_devuelveListaVacia() {
		// Given
		List<Usuario> entidades = List.of();
		when(usuarioRepository.findAll(Sort.by("username"))).thenReturn(entidades);
		when(usuarioMapper.toResponseList(anyList())).thenReturn(List.of());

		// When
		List<UsuarioDTOResponse> resultado = usuarioService.listar();

		// Then
		assertTrue(resultado.isEmpty());
	}

	// ------------------------------------------------------------------
	// obtenerPorId
	// ------------------------------------------------------------------

	// Camino feliz: id existe -> devuelve el DTO mapeado
	@Test
	void obtenerPorId_idExistente_devuelveResponse() {
		// Given
		Usuario entidad = DataProviderUsuario.usuarioValido();
		UsuarioDTOResponse esperado = responseDe(1L, "pepe");
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(usuarioMapper.toResponse(entidad)).thenReturn(esperado);

		// When
		UsuarioDTOResponse resultado = usuarioService.obtenerPorId(1L);

		// Then
		assertEquals("pepe", resultado.getUsername());
		verify(usuarioRepository).findById(1L);
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void obtenerPorId_idInexistente_lanzaNoSuchElement() {
		// Given
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class, () -> usuarioService.obtenerPorId(99L));
	}

	// ------------------------------------------------------------------
	// crearUsuario
	// ------------------------------------------------------------------

	// Camino feliz: request válido -> valida, codifica password, activa y guarda
	@Test
	void crearUsuario_requestValido_guardaYCodificaPassword() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		Usuario entidad = DataProviderUsuario.usuarioValido();
		UsuarioDTOResponse respuesta = responseDe(10L, "nuevo.usuario");
		when(usuarioRepository.existsByUsername("nuevo.usuario")).thenReturn(false);
		when(usuarioMapper.toEntity(request)).thenReturn(entidad);
		when(passwordEncoder.encode("secreto123")).thenReturn("$2a$10$nuevo");
		when(usuarioRepository.save(entidad)).thenReturn(entidad);
		when(usuarioMapper.toResponse(entidad)).thenReturn(respuesta);

		// When
		UsuarioDTOResponse resultado = usuarioService.crearUsuario(request);

		// Then
		assertEquals(10L, resultado.getId());
		verify(passwordEncoder).encode("secreto123");
		ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(captor.capture());
		Usuario guardado = captor.getValue();
		assertTrue(guardado.isActivo());
		assertEquals("nuevo.usuario", guardado.getUsername());
		assertEquals("$2a$10$nuevo", guardado.getPassword());
		assertEquals(Rol.CAJERO, guardado.getRol());
	}

	// Excepción: username vacío -> ValidacionDatos lanza antes de tocar la BD
	@Test
	void crearUsuario_usernameVacio_lanzaExcepcion() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		request.setUsername("   ");

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> usuarioService.crearUsuario(request));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// Excepción: username con caracteres no permitidos -> ValidacionDatos lanza
	@Test
	void crearUsuario_usernameInvalidoCaracteres_lanzaExcepcion() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		request.setUsername("user name");

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> usuarioService.crearUsuario(request));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// Excepción: password menor de 4 caracteres -> ValidacionDatos lanza
	@Test
	void crearUsuario_passwordCorta_lanzaExcepcion() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		request.setPassword("abc");

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> usuarioService.crearUsuario(request));
		verify(passwordEncoder, never()).encode(anyString());
	}

	// Excepción: nombre vacío -> ValidacionDatos lanza
	@Test
	void crearUsuario_nombreVacio_lanzaExcepcion() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		request.setNombreCompleto("");

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> usuarioService.crearUsuario(request));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// Excepción: rol nulo -> ValidacionDatos.requerido lanza
	@Test
	void crearUsuario_rolNulo_lanzaExcepcion() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		request.setRol(null);

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> usuarioService.crearUsuario(request));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// Excepción: username ya registrado -> rechaza sin guardar
	@Test
	void crearUsuario_usernameDuplicado_lanzaExcepcion() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		when(usuarioRepository.existsByUsername("nuevo.usuario")).thenReturn(true);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> usuarioService.crearUsuario(request));
		assertEquals("Ya existe un usuario con ese nombre", error.getMessage());
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// ------------------------------------------------------------------
	// actualizar
	// ------------------------------------------------------------------

	// Camino feliz: trae password nueva -> se valida y se codifica
	@Test
	void actualizar_conPassword_codificaYGuarda() {
		// Given
		Usuario entidad = DataProviderUsuario.usuarioValido();
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		UsuarioDTOResponse respuesta = responseDe(1L, "pepe");
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(passwordEncoder.encode("secreto123")).thenReturn("$2a$10$nuevo");
		when(usuarioRepository.save(entidad)).thenReturn(entidad);
		when(usuarioMapper.toResponse(entidad)).thenReturn(respuesta);

		// When
		UsuarioDTOResponse resultado = usuarioService.actualizar(1L, request);

		// Then
		assertEquals(1L, resultado.getId());
		verify(passwordEncoder).encode("secreto123");
		assertEquals("$2a$10$nuevo", entidad.getPassword());
	}

	// Camino feliz: sin password -> no se codifica ni se toca la contraseña
	@Test
	void actualizar_sinPassword_noTocaElPassword() {
		// Given
		Usuario entidad = DataProviderUsuario.usuarioValido();
		UsuarioDTORequest request = DataProviderUsuario.requestSinPassword();
		UsuarioDTOResponse respuesta = responseDe(1L, "pepe");
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(entidad));
		when(usuarioRepository.save(entidad)).thenReturn(entidad);
		when(usuarioMapper.toResponse(entidad)).thenReturn(respuesta);

		// When
		usuarioService.actualizar(1L, request);

		// Then
		verify(passwordEncoder, never()).encode(anyString());
		assertEquals("$2a$10$hashfalso", entidad.getPassword());
	}

	// Excepción: nombre vacío -> ValidacionDatos lanza antes de buscar
	@Test
	void actualizar_nombreVacio_lanzaExcepcion() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		request.setNombreCompleto("  ");

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizar(1L, request));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// Excepción: rol nulo -> ValidacionDatos.requerido lanza antes de buscar
	@Test
	void actualizar_rolNulo_lanzaExcepcion() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		request.setRol(null);

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizar(1L, request));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// Excepción: password nueva inválida -> ValidacionDatos lanza antes de buscar
	@Test
	void actualizar_passwordInvalida_lanzaExcepcion() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		request.setPassword("abc");

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizar(1L, request));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void actualizar_idInexistente_lanzaNoSuchElement() {
		// Given
		UsuarioDTORequest request = DataProviderUsuario.requestValido();
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class, () -> usuarioService.actualizar(99L, request));
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// Excepción: desactivar al único ADMIN activo -> rechaza sin guardar
	@Test
	void actualizar_desactivarUnicoAdmin_lanzaExcepcion() {
		// Given
		Usuario admin = DataProviderUsuario.usuarioAdmin();
		UsuarioDTORequest request = DataProviderUsuario.requestDesactivado();
		when(usuarioRepository.findById(2L)).thenReturn(Optional.of(admin));
		when(usuarioRepository.countByActivoTrueAndRol(Rol.ADMIN)).thenReturn(1L);

		// When / Then
		IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
				() -> usuarioService.actualizar(2L, request));
		assertEquals("No se puede desactivar al único administrador activo", error.getMessage());
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// ------------------------------------------------------------------
	// alternarActivo
	// ------------------------------------------------------------------

	// Camino feliz: cajero inactivo -> se activa sin preguntar por ADMINs
	@Test
	void alternarActivo_cajeroInactivo_loActiva() {
		// Given
		Usuario cajero = DataProviderUsuario.usuarioValido();
		cajero.setActivo(false);
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cajero));
		when(usuarioRepository.save(cajero)).thenReturn(cajero);
		when(usuarioMapper.toResponse(cajero)).thenReturn(responseDe(1L, "pepe"));

		// When
		usuarioService.alternarActivo(1L);

		// Then
		assertTrue(cajero.isActivo());
		verify(usuarioRepository, never()).countByActivoTrueAndRol(any(Rol.class));
	}

	// Camino feliz: cajero activo -> se desactiva (no es ADMIN, sin consultar count)
	@Test
	void alternarActivo_cajeroActivo_loDesactiva() {
		// Given
		Usuario cajero = DataProviderUsuario.usuarioValido();
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cajero));
		when(usuarioRepository.save(cajero)).thenReturn(cajero);
		when(usuarioMapper.toResponse(cajero)).thenReturn(responseDe(1L, "pepe"));

		// When
		usuarioService.alternarActivo(1L);

		// Then
		assertFalse(cajero.isActivo());
		verify(usuarioRepository, never()).countByActivoTrueAndRol(any(Rol.class));
	}

	// Excepción: único ADMIN activo -> rechaza el apagado sin guardar
	@Test
	void alternarActivo_unicoAdminActivo_lanzaExcepcion() {
		// Given
		Usuario admin = DataProviderUsuario.usuarioAdmin();
		when(usuarioRepository.findById(2L)).thenReturn(Optional.of(admin));
		when(usuarioRepository.countByActivoTrueAndRol(Rol.ADMIN)).thenReturn(1L);

		// When / Then
		assertThrows(IllegalArgumentException.class, () -> usuarioService.alternarActivo(2L));
		assertTrue(admin.isActivo());
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	// Excepción: id inexistente -> NoSuchElementException
	@Test
	void alternarActivo_idInexistente_lanzaNoSuchElement() {
		// Given
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

		// When / Then
		assertThrows(NoSuchElementException.class, () -> usuarioService.alternarActivo(99L));
	}

	// ------------------------------------------------------------------
	// helpers
	// ------------------------------------------------------------------

	private UsuarioDTOResponse responseDe(Long id, String username) {
		UsuarioDTOResponse response = new UsuarioDTOResponse();
		response.setId(id);
		response.setUsername(username);
		response.setActivo(true);
		return response;
	}

}
