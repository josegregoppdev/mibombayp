package com.mibombay.empresa.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mibombay.empresa.dto.ClienteDTO;
import com.mibombay.empresa.mapper.ClienteMapper;
import com.mibombay.empresa.model.Cliente;
import com.mibombay.empresa.repository.ClienteRepository;
import com.mibombay.empresa.util.ValidacionDatos;

@Service
public class ClienteService {

	private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

	private final ClienteRepository clienteRepository;
	private final ClienteMapper clienteMapper;

	public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
		this.clienteRepository = clienteRepository;
		this.clienteMapper = clienteMapper;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public List<ClienteDTO> listar() {
		log.debug("Listando clientes");
		List<ClienteDTO> clientes = clienteMapper.toDTOList(clienteRepository.findAll(Sort.by("nombre")));
		log.debug("Clientes encontrados: {}", clientes.size());
		return clientes;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ClienteDTO obtenerPorId(Long id) {
		log.debug("Obteniendo cliente id={}", id);
		return clienteMapper.toDTO(obtenerEntidad(id));
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ClienteDTO crear(ClienteDTO dto) {
		log.debug("Creando cliente nombre={}", dto.getNombre());
		String nombre = ValidacionDatos.nombre(dto.getNombre());
		String dniNit = validarDniNit(dto.getDniNit());
		validarOpcionales(dto);

		if (clienteRepository.existsByDniNitIgnoreCase(dniNit)) {
			log.debug("Alta rechazada, DNI/NIT duplicado: {}", dniNit);
			throw new IllegalArgumentException("Ya existe un cliente con ese DNI/NIT");
		}

		Cliente cliente = clienteMapper.toEntity(dto);
		cliente.setNombre(nombre);
		cliente.setDniNit(dniNit);
		cliente.setActivo(true);
		ClienteDTO respuesta = clienteMapper.toDTO(clienteRepository.save(cliente));
		log.info("Cliente creado: id={} nombre={} dniNit={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.getDniNit());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ClienteDTO actualizar(Long id, ClienteDTO dto) {
		log.debug("Actualizando cliente id={}", id);
		String nombre = ValidacionDatos.nombre(dto.getNombre());
		String dniNit = validarDniNit(dto.getDniNit());
		validarOpcionales(dto);

		Cliente cliente = obtenerEntidad(id);
		if (!cliente.getDniNit().equalsIgnoreCase(dniNit)
				&& clienteRepository.existsByDniNitIgnoreCase(dniNit)) {
			log.debug("Actualización rechazada, DNI/NIT duplicado: {}", dniNit);
			throw new IllegalArgumentException("Ya existe un cliente con ese DNI/NIT");
		}

		cliente.setNombre(nombre);
		cliente.setDniNit(dniNit);
		cliente.setApellido(dto.getApellido());
		cliente.setDireccion(dto.getDireccion());
		cliente.setTelefono(dto.getTelefono());
		cliente.setEmail(dto.getEmail());
		cliente.setActivo(dto.isActivo());
		ClienteDTO respuesta = clienteMapper.toDTO(clienteRepository.save(cliente));
		log.info("Cliente actualizado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ClienteDTO alternarActivo(Long id) {
		log.debug("Cambiando estado cliente id={}", id);
		Cliente cliente = obtenerEntidad(id);
		cliente.setActivo(!cliente.isActivo());
		ClienteDTO respuesta = clienteMapper.toDTO(clienteRepository.save(cliente));
		log.info("Estado cambiado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		return respuesta;
	}

	private Cliente obtenerEntidad(Long id) {
		return clienteRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Cliente no encontrado: " + id));
	}

	private String validarDniNit(String valor) {
		String dniNit = ValidacionDatos.requerido(valor, "El DNI/NIT");
		ValidacionDatos.longitudMaxima(dniNit, 15, "El DNI/NIT");
		ValidacionDatos.sinInyeccion(dniNit, "El DNI/NIT");
		return dniNit;
	}

	private void validarOpcionales(ClienteDTO dto) {
		ValidacionDatos.longitudMaxima(dto.getApellido(), 100, "El apellido");
		ValidacionDatos.longitudMaxima(dto.getDireccion(), 100, "La dirección");
		ValidacionDatos.longitudMaxima(dto.getTelefono(), 15, "El teléfono");
		ValidacionDatos.longitudMaxima(dto.getEmail(), 100, "El email");
	}

}
