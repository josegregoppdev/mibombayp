package com.mibombay.empresa.service;

import java.util.List;

import com.mibombay.empresa.dto.UsuarioDTORequest;
import com.mibombay.empresa.model.Rol;
import com.mibombay.empresa.model.Usuario;

/**
 * Fábrica de datos falsos para los tests de UsuarioService.
 * Solo métodos estáticos: se llama como DataProviderUsuario.usuarioValido(), etc.
 */
public final class DataProviderUsuario {

	private DataProviderUsuario() {
	}

	// Usuario base válido (rol CAJERO)
	public static Usuario usuarioValido() {
		Usuario usuario = new Usuario();
		usuario.setId(1L);
		usuario.setUsername("pepe");
		usuario.setPassword("$2a$10$hashfalso");
		usuario.setNombreCompleto("Pepe Argento");
		usuario.setRol(Rol.CAJERO);
		usuario.setActivo(true);
		return usuario;
	}

	// Usuario ADMIN activo (para regla de único administrador)
	public static Usuario usuarioAdmin() {
		Usuario usuario = usuarioValido();
		usuario.setId(2L);
		usuario.setUsername("admin");
		usuario.setNombreCompleto("Admin General");
		usuario.setRol(Rol.ADMIN);
		return usuario;
	}

	// Lista de 3 usuarios variados (ADMIN, CAJERO, DEV) para listar()
	public static List<Usuario> listaUsuarios() {
		Usuario admin = usuarioAdmin();

		Usuario cajero = usuarioValido();

		Usuario dev = usuarioValido();
		dev.setId(3L);
		dev.setUsername("dev");
		dev.setNombreCompleto("Dev Local");
		dev.setRol(Rol.DEV);

		return List.of(admin, cajero, dev);
	}

	// Request completo y válido (base para los tests de crear/actualizar)
	public static UsuarioDTORequest requestValido() {
		UsuarioDTORequest request = new UsuarioDTORequest();
		request.setUsername("nuevo.usuario");
		request.setPassword("secreto123");
		request.setNombreCompleto("Usuario Nuevo");
		request.setRol(Rol.CAJERO);
		request.setActivo(true);
		return request;
	}

	// Request válido sin password (para probar que actualizar no toca la contraseña)
	public static UsuarioDTORequest requestSinPassword() {
		UsuarioDTORequest request = requestValido();
		request.setPassword(null);
		return request;
	}

	// Request válido con activo=false (para probar la regla del único ADMIN activo)
	public static UsuarioDTORequest requestDesactivado() {
		UsuarioDTORequest request = requestValido();
		request.setActivo(false);
		return request;
	}

}
