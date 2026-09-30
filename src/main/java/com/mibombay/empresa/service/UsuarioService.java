package com.mibombay.empresa.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mibombay.empresa.dto.UsuarioDTORequest;
import com.mibombay.empresa.dto.UsuarioDTOResponse;
import com.mibombay.empresa.mapper.UsuarioMapper;
import com.mibombay.empresa.model.Rol;
import com.mibombay.empresa.model.Usuario;
import com.mibombay.empresa.repository.UsuarioRepository;
import com.mibombay.empresa.util.ValidacionDatos;

@Service
public class UsuarioService {

	private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final UsuarioMapper usuarioMapper;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
			UsuarioMapper usuarioMapper) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.usuarioMapper = usuarioMapper;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public List<UsuarioDTOResponse> listar() {
		log.debug("Listando usuarios");
		List<UsuarioDTOResponse> usuarios = usuarioMapper
				.toResponseList(usuarioRepository.findAll(Sort.by("username")));
		log.debug("Usuarios encontrados: {}", usuarios.size());
		return usuarios;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public UsuarioDTOResponse obtenerPorId(Long id) {
		log.debug("Obteniendo usuario id={}", id);
		return usuarioMapper.toResponse(obtenerEntidad(id));
	}

	@Transactional
	public UsuarioDTOResponse crearUsuario(UsuarioDTORequest request) {

		log.debug("Creando usuario username={} rol={}", request.getUsername(), request.getRol());
		String username = ValidacionDatos.username(request.getUsername());
		String password = ValidacionDatos.password(request.getPassword());
		String nombreCompleto = ValidacionDatos.nombre(request.getNombreCompleto());
		Rol rol = request.getRol();
		ValidacionDatos.noNulo(rol, "El rol");

		if (usuarioRepository.existsByUsername(username)) {
			log.debug("Alta rechazada, username duplicado: {}", username);
			throw new IllegalArgumentException("Ya existe un usuario con ese nombre");
		}

		Usuario usuario = usuarioMapper.toEntity(request);
		usuario.setUsername(username);
		usuario.setPassword(passwordEncoder.encode(password));
		usuario.setNombreCompleto(nombreCompleto);
		usuario.setRol(rol);
		usuario.setActivo(true);
		UsuarioDTOResponse respuesta = usuarioMapper.toResponse(usuarioRepository.save(usuario));
		log.info("Usuario creado: id={} username={} rol={}", respuesta.getId(), respuesta.getUsername(),
				respuesta.getRol());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public UsuarioDTOResponse actualizar(Long id, UsuarioDTORequest request) {
		log.debug("Actualizando usuario id={}", id);
		String nombreCompleto = ValidacionDatos.nombre(request.getNombreCompleto());
		Rol rol = request.getRol();
		ValidacionDatos.noNulo(rol, "El rol");
		String password = request.getPassword();
		if (password != null && !password.isBlank()) {
			password = ValidacionDatos.password(password);
		}

		Usuario usuario = obtenerEntidad(id);
		if (!request.isActivo() && !puedeDesactivarse(usuario)) {
			log.debug("Actualización rechazada, único ADMIN activo id={}", id);
			throw new IllegalArgumentException("No se puede desactivar al único administrador activo");
		}

		usuario.setNombreCompleto(nombreCompleto);
		usuario.setRol(rol);
		usuario.setActivo(request.isActivo());
		if (password != null && !password.isBlank()) {
			usuario.setPassword(passwordEncoder.encode(password));
		}
		UsuarioDTOResponse respuesta = usuarioMapper.toResponse(usuarioRepository.save(usuario));
		log.info("Usuario actualizado: id={} username={} rol={} activo={}", respuesta.getId(),
				respuesta.getUsername(), respuesta.getRol(), respuesta.isActivo());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public UsuarioDTOResponse alternarActivo(Long id) {
		log.debug("Cambiando estado usuario id={}", id);
		Usuario usuario = obtenerEntidad(id);
		if (usuario.isActivo() && !puedeDesactivarse(usuario)) {
			log.debug("Cambio de estado rechazado, único ADMIN activo id={}", id);
			throw new IllegalArgumentException("No se puede desactivar al único administrador activo");
		}
		usuario.setActivo(!usuario.isActivo());
		UsuarioDTOResponse respuesta = usuarioMapper.toResponse(usuarioRepository.save(usuario));
		log.info("Estado cambiado: id={} username={} rol={} activo={}", respuesta.getId(),
				respuesta.getUsername(), respuesta.getRol(), respuesta.isActivo());
		return respuesta;
	}

	private Usuario obtenerEntidad(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + id));
	}

	private boolean puedeDesactivarse(Usuario usuario) {
		if (usuario.getRol() != Rol.ADMIN) {
			return true;
		}
		return usuarioRepository.countByActivoTrueAndRol(Rol.ADMIN) > 1;
	}

}
