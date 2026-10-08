package com.mibombay.empresa.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mibombay.empresa.dto.ProveedorDTO;
import com.mibombay.empresa.mapper.ProveedorMapper;
import com.mibombay.empresa.model.Proveedor;
import com.mibombay.empresa.repository.ProveedorRepository;
import com.mibombay.empresa.util.ValidacionDatos;

@Service
public class ProveedorService {

	private static final Logger log = LoggerFactory.getLogger(ProveedorService.class);

	private final ProveedorRepository proveedorRepository;
	private final ProveedorMapper proveedorMapper;

	public ProveedorService(ProveedorRepository proveedorRepository, ProveedorMapper proveedorMapper) {
		this.proveedorRepository = proveedorRepository;
		this.proveedorMapper = proveedorMapper;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public List<ProveedorDTO> listar() {
		log.debug("Listando proveedores");
		List<ProveedorDTO> proveedores = proveedorMapper
				.toDTOList(proveedorRepository.findAll(Sort.by("nombre")));
		log.debug("Proveedores encontrados: {}", proveedores.size());
		return proveedores;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProveedorDTO obtenerPorId(Long id) {
		log.debug("Obteniendo proveedor id={}", id);
		return proveedorMapper.toDTO(obtenerEntidad(id));
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProveedorDTO crear(ProveedorDTO dto) {
		log.debug("Creando proveedor nombre={}", dto.getNombre());
		String nombre = ValidacionDatos.nombre(dto.getNombre());
		String dniNit = validarDniNit(dto.getDniNit());
		validarOpcionales(dto);

		if (proveedorRepository.existsByDniNitIgnoreCase(dniNit)) {
			log.debug("Alta rechazada, DNI/NIT duplicado: {}", dniNit);
			throw new IllegalArgumentException("Ya existe un proveedor con ese DNI/NIT");
		}

		Proveedor proveedor = proveedorMapper.toEntity(dto);
		proveedor.setNombre(nombre);
		proveedor.setDniNit(dniNit);
		proveedor.setActivo(true);
		ProveedorDTO respuesta = proveedorMapper.toDTO(proveedorRepository.save(proveedor));
		log.info("Proveedor creado: id={} nombre={} dniNit={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.getDniNit());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProveedorDTO actualizar(Long id, ProveedorDTO dto) {
		log.debug("Actualizando proveedor id={}", id);
		String nombre = ValidacionDatos.nombre(dto.getNombre());
		String dniNit = validarDniNit(dto.getDniNit());
		validarOpcionales(dto);

		Proveedor proveedor = obtenerEntidad(id);
		if (!proveedor.getDniNit().equalsIgnoreCase(dniNit)
				&& proveedorRepository.existsByDniNitIgnoreCase(dniNit)) {
			log.debug("Actualización rechazada, DNI/NIT duplicado: {}", dniNit);
			throw new IllegalArgumentException("Ya existe un proveedor con ese DNI/NIT");
		}

		proveedor.setNombre(nombre);
		proveedor.setDniNit(dniNit);
		proveedor.setApellido(dto.getApellido());
		proveedor.setDireccion(dto.getDireccion());
		proveedor.setTelefono(dto.getTelefono());
		proveedor.setEmail(dto.getEmail());
		proveedor.setActivo(dto.isActivo());
		ProveedorDTO respuesta = proveedorMapper.toDTO(proveedorRepository.save(proveedor));
		log.info("Proveedor actualizado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProveedorDTO alternarActivo(Long id) {
		log.debug("Cambiando estado proveedor id={}", id);
		Proveedor proveedor = obtenerEntidad(id);
		proveedor.setActivo(!proveedor.isActivo());
		ProveedorDTO respuesta = proveedorMapper.toDTO(proveedorRepository.save(proveedor));
		log.info("Estado cambiado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		return respuesta;
	}

	private Proveedor obtenerEntidad(Long id) {
		return proveedorRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Proveedor no encontrado: " + id));
	}

	private String validarDniNit(String valor) {
		String dniNit = ValidacionDatos.requerido(valor, "El DNI/NIT");
		ValidacionDatos.longitudMaxima(dniNit, 15, "El DNI/NIT");
		ValidacionDatos.sinInyeccion(dniNit, "El DNI/NIT");
		return dniNit;
	}

	private void validarOpcionales(ProveedorDTO dto) {
		ValidacionDatos.longitudMaxima(dto.getApellido(), 100, "El apellido");
		ValidacionDatos.longitudMaxima(dto.getDireccion(), 100, "La dirección");
		ValidacionDatos.longitudMaxima(dto.getTelefono(), 15, "El teléfono");
		ValidacionDatos.longitudMaxima(dto.getEmail(), 100, "El email");
	}

}
