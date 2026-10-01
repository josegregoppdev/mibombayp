package com.mibombay.empresa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mibombay.empresa.dto.UsuarioDTOResponse;
import com.mibombay.empresa.mapper.UsuarioMapper;
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
