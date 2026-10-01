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

import com.mibombay.empresa.dto.DetalleRecetaDTO;
import com.mibombay.empresa.dto.RecetaDTO;
import com.mibombay.empresa.mapper.DetalleRecetaMapper;
import com.mibombay.empresa.mapper.RecetaMapper;
import com.mibombay.empresa.model.DetalleReceta;
import com.mibombay.empresa.model.Ingrediente;
import com.mibombay.empresa.model.Receta;
import com.mibombay.empresa.model.UnidadMedida;
import com.mibombay.empresa.repository.DetalleRecetaRepository;
import com.mibombay.empresa.repository.IngredienteRepository;
import com.mibombay.empresa.repository.RecetaRepository;
import com.mibombay.empresa.util.ValidacionDatos;

@Service
public class RecetaService {

	private static final Logger log = LoggerFactory.getLogger(RecetaService.class);

	private final RecetaRepository recetaRepository;
	private final DetalleRecetaRepository detalleRepository;
	private final IngredienteRepository ingredienteRepository;
	private final RecetaMapper recetaMapper;
	private final DetalleRecetaMapper detalleMapper;

	public RecetaService(RecetaRepository recetaRepository, DetalleRecetaRepository detalleRepository,
			IngredienteRepository ingredienteRepository, RecetaMapper recetaMapper,
			DetalleRecetaMapper detalleMapper) {
		this.recetaRepository = recetaRepository;
		this.detalleRepository = detalleRepository;
		this.ingredienteRepository = ingredienteRepository;
		this.recetaMapper = recetaMapper;
		this.detalleMapper = detalleMapper;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public List<RecetaDTO> listar() {
		log.debug("Listando recetas");
		List<RecetaDTO> recetas = recetaMapper.toDTOList(recetaRepository.findAll(Sort.by("nombre")));
		log.debug("Recetas encontradas: {}", recetas.size());
		return recetas;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public RecetaDTO obtenerPorId(Long id) {
		log.debug("Obteniendo receta id={}", id);
		return recetaMapper.toDTO(obtenerEntidad(id));
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public List<DetalleRecetaDTO> listarDetalles(Long recetaId) {
		log.debug("Listando detalles recetaId={}", recetaId);
		obtenerEntidad(recetaId);
		return detalleMapper.toDTOList(detalleRepository.findByRecetaId(recetaId));
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public RecetaDTO crear(RecetaDTO dto) {
		log.debug("Creando receta nombre={}", dto.getNombre());
		String nombre = ValidacionDatos.nombre(dto.getNombre());

		if (recetaRepository.existsByNombreIgnoreCase(nombre)) {
			log.debug("Alta rechazada, nombre duplicado: {}", nombre);
			throw new IllegalArgumentException("Ya existe una receta con ese nombre");
		}

		Receta receta = recetaMapper.toEntity(dto);
		receta.setNombre(nombre);
		receta.setCosto(BigDecimal.ZERO);
		receta.setActivo(true);
		RecetaDTO respuesta = recetaMapper.toDTO(recetaRepository.save(receta));
		log.info("Receta creada: id={} nombre={}", respuesta.getId(), respuesta.getNombre());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public RecetaDTO actualizar(Long id, RecetaDTO dto) {
		log.debug("Actualizando receta id={}", id);
		String nombre = ValidacionDatos.nombre(dto.getNombre());

		Receta receta = obtenerEntidad(id);
		if (!receta.getNombre().equalsIgnoreCase(nombre)
				&& recetaRepository.existsByNombreIgnoreCase(nombre)) {
			log.debug("Actualización rechazada, nombre duplicado: {}", nombre);
			throw new IllegalArgumentException("Ya existe una receta con ese nombre");
		}

		receta.setNombre(nombre);
		receta.setActivo(dto.isActivo());
		RecetaDTO respuesta = recetaMapper.toDTO(recetaRepository.save(receta));
		log.info("Receta actualizada: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public RecetaDTO alternarActivo(Long id) {
		log.debug("Cambiando estado receta id={}", id);
		Receta receta = obtenerEntidad(id);
		receta.setActivo(!receta.isActivo());
		RecetaDTO respuesta = recetaMapper.toDTO(recetaRepository.save(receta));
		log.info("Estado cambiado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public DetalleRecetaDTO agregarDetalle(DetalleRecetaDTO dto) {
		log.debug("Agregando detalle recetaId={} ingredienteId={}", dto.getRecetaId(), dto.getIngredienteId());
		Receta receta = obtenerEntidad(dto.getRecetaId());
		Ingrediente ingrediente = ingredienteRepository.findById(dto.getIngredienteId())
				.orElseThrow(() -> new NoSuchElementException("Ingrediente no encontrado: " + dto.getIngredienteId()));
		if (!ingrediente.isActivo()) {
			log.debug("Detalle rechazado, ingrediente inactivo id={}", ingrediente.getId());
			throw new IllegalArgumentException("El ingrediente está inactivo");
		}
		BigDecimal cantidad = ValidacionDatos.stock(dto.getCantidad(), "La cantidad");
		if (cantidad.signum() <= 0) {
			log.debug("Detalle rechazado, cantidad no positiva");
			throw new IllegalArgumentException("La cantidad: debe ser mayor a cero");
		}
		UnidadMedida unidadMedida = dto.getUnidadMedida();
		ValidacionDatos.requerido(unidadMedida, "La unidad de medida");

		if (detalleRepository.existsByRecetaIdAndIngredienteId(receta.getId(), ingrediente.getId())) {
			log.debug("Detalle rechazado, ingrediente duplicado en receta");
			throw new IllegalArgumentException("El ingrediente ya está en la receta");
		}

		DetalleReceta detalle = detalleMapper.toEntity(dto);
		detalle.setReceta(receta);
		detalle.setIngrediente(ingrediente);
		detalle.setCantidad(cantidad);
		detalle.setUnidadMedida(unidadMedida);
		DetalleRecetaDTO respuesta = detalleMapper.toDTO(detalleRepository.save(detalle));
		recalcularCosto(receta.getId());
		log.info("Detalle agregado: id={} recetaId={} ingrediente={}", respuesta.getId(), receta.getId(),
				ingrediente.getNombre());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public void eliminarDetalle(Long detalleId) {
		log.debug("Eliminando detalle id={}", detalleId);
		DetalleReceta detalle = detalleRepository.findById(detalleId)
				.orElseThrow(() -> new NoSuchElementException("Detalle no encontrado: " + detalleId));
		Long recetaId = detalle.getReceta().getId();
		detalleRepository.delete(detalle);
		recalcularCosto(recetaId);
		log.info("Detalle eliminado: id={} recetaId={}", detalleId, recetaId);
	}

	private Receta obtenerEntidad(Long id) {
		return recetaRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Receta no encontrada: " + id));
	}

	private void recalcularCosto(Long recetaId) {
		List<DetalleReceta> detalles = detalleRepository.findByRecetaId(recetaId);
		BigDecimal costo = BigDecimal.ZERO;
		for (DetalleReceta d : detalles) {
			BigDecimal valorCompra = d.getIngrediente().getValorCompra();
			if (valorCompra != null) {
				costo = costo.add(d.getCantidad().multiply(valorCompra));
			}
		}
		Receta receta = obtenerEntidad(recetaId);
		receta.setCosto(costo);
		recetaRepository.save(receta);
		log.debug("Costo recalculado recetaId={} costo={}", recetaId, costo);
	}

}
