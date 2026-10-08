package com.mibombay.empresa.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mibombay.empresa.dto.IngredienteDTO;
import com.mibombay.empresa.mapper.IngredienteMapper;
import com.mibombay.empresa.model.Ingrediente;
import com.mibombay.empresa.model.UnidadMedida;
import com.mibombay.empresa.repository.IngredienteRepository;
import com.mibombay.empresa.util.ValidacionDatos;

@Service
public class IngredienteService {

	private static final Logger log = LoggerFactory.getLogger(IngredienteService.class);

	private final IngredienteRepository ingredienteRepository;
	private final IngredienteMapper ingredienteMapper;

	public IngredienteService(IngredienteRepository ingredienteRepository, IngredienteMapper ingredienteMapper) {
		this.ingredienteRepository = ingredienteRepository;
		this.ingredienteMapper = ingredienteMapper;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public List<IngredienteDTO> listar() {
		log.debug("Listando ingredientes");
		List<IngredienteDTO> ingredientes = ingredienteMapper
				.toDTOList(ingredienteRepository.findAll(Sort.by("nombre")));
		log.debug("Ingredientes encontrados: {}", ingredientes.size());
		return ingredientes;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public IngredienteDTO obtenerPorId(Long id) {
		log.debug("Obteniendo ingrediente id={}", id);
		return ingredienteMapper.toDTO(obtenerEntidad(id));
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public IngredienteDTO crear(IngredienteDTO dto) {
		log.debug("Creando ingrediente nombre={} unidad={}", dto.getNombre(), dto.getUnidadMedida());
		String nombre = ValidacionDatos.nombre(dto.getNombre());
		UnidadMedida unidadMedida = dto.getUnidadMedida();
		ValidacionDatos.requerido(unidadMedida, "La unidad de medida");
		BigDecimal stockActual = ValidacionDatos.stock(dto.getStockActual(), "El stock actual");
		BigDecimal stockMinimo = ValidacionDatos.stock(dto.getStockMinimo(), "El stock mínimo");
		BigDecimal valorCompra = ValidacionDatos.stock(dto.getValorCompra(), "El valor de compra");
		BigDecimal valorVenta = ValidacionDatos.stock(dto.getValorVenta(), "El valor de venta");
		BigDecimal porcionAdicional = ValidacionDatos.stock(dto.getPorcionAdicional(), "La porción adicional");
		BigDecimal precioAdicional = ValidacionDatos.stock(dto.getPrecioAdicional(), "El precio adicional");
		validarMargen(valorCompra, valorVenta);

		if (ingredienteRepository.existsByNombreIgnoreCase(nombre)) {
			log.debug("Alta rechazada, nombre duplicado: {}", nombre);
			throw new IllegalArgumentException("Ya existe un ingrediente con ese nombre");
		}

		Ingrediente ingrediente = ingredienteMapper.toEntity(dto);
		ingrediente.setNombre(nombre);
		ingrediente.setUnidadMedida(unidadMedida);
		ingrediente.setStockActual(stockActual);
		ingrediente.setStockMinimo(stockMinimo);
		ingrediente.setValorCompra(valorCompra);
		ingrediente.setValorVenta(valorVenta);
		ingrediente.setPorcionAdicional(porcionAdicional);
		ingrediente.setPrecioAdicional(precioAdicional);
		ingrediente.setEsAdicional(dto.isEsAdicional());
		ingrediente.setActivo(true);
		IngredienteDTO respuesta = ingredienteMapper.toDTO(ingredienteRepository.save(ingrediente));
		log.info("Ingrediente creado: id={} nombre={} unidad={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.getUnidadMedida());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public IngredienteDTO actualizar(Long id, IngredienteDTO dto) {
		log.debug("Actualizando ingrediente id={}", id);
		String nombre = ValidacionDatos.nombre(dto.getNombre());
		UnidadMedida unidadMedida = dto.getUnidadMedida();
		ValidacionDatos.requerido(unidadMedida, "La unidad de medida");
		BigDecimal stockActual = ValidacionDatos.stock(dto.getStockActual(), "El stock actual");
		BigDecimal stockMinimo = ValidacionDatos.stock(dto.getStockMinimo(), "El stock mínimo");
		BigDecimal valorCompra = ValidacionDatos.stock(dto.getValorCompra(), "El valor de compra");
		BigDecimal valorVenta = ValidacionDatos.stock(dto.getValorVenta(), "El valor de venta");
		BigDecimal porcionAdicional = ValidacionDatos.stock(dto.getPorcionAdicional(), "La porción adicional");
		BigDecimal precioAdicional = ValidacionDatos.stock(dto.getPrecioAdicional(), "El precio adicional");
		validarMargen(valorCompra, valorVenta);

		Ingrediente ingrediente = obtenerEntidad(id);
		if (!ingrediente.getNombre().equalsIgnoreCase(nombre)
				&& ingredienteRepository.existsByNombreIgnoreCase(nombre)) {
			log.debug("Actualización rechazada, nombre duplicado: {}", nombre);
			throw new IllegalArgumentException("Ya existe un ingrediente con ese nombre");
		}

		ingrediente.setNombre(nombre);
		ingrediente.setUnidadMedida(unidadMedida);
		ingrediente.setStockActual(stockActual);
		ingrediente.setStockMinimo(stockMinimo);
		ingrediente.setValorCompra(valorCompra);
		ingrediente.setValorVenta(valorVenta);
		ingrediente.setPorcionAdicional(porcionAdicional);
		ingrediente.setPrecioAdicional(precioAdicional);
		ingrediente.setEsAdicional(dto.isEsAdicional());
		ingrediente.setActivo(dto.isActivo());
		IngredienteDTO respuesta = ingredienteMapper.toDTO(ingredienteRepository.save(ingrediente));
		log.info("Ingrediente actualizado: id={} nombre={} unidad={} activo={}", respuesta.getId(),
				respuesta.getNombre(), respuesta.getUnidadMedida(), respuesta.isActivo());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public IngredienteDTO alternarActivo(Long id) {
		log.debug("Cambiando estado ingrediente id={}", id);
		Ingrediente ingrediente = obtenerEntidad(id);
		ingrediente.setActivo(!ingrediente.isActivo());
		IngredienteDTO respuesta = ingredienteMapper.toDTO(ingredienteRepository.save(ingrediente));
		log.info("Estado cambiado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		return respuesta;
	}

	private Ingrediente obtenerEntidad(Long id) {
		return ingredienteRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Ingrediente no encontrado: " + id));
	}

	private void validarMargen(BigDecimal valorCompra, BigDecimal valorVenta) {
		if (valorVenta.compareTo(valorCompra) < 0) {
			log.debug("Operación rechazada, venta menor que compra");
			throw new IllegalArgumentException("El valor de venta no puede ser menor al valor de compra");
		}
	}

}
