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

import com.mibombay.empresa.dto.ProductoConRecetaDTO;
import com.mibombay.empresa.mapper.ProductoConRecetaMapper;
import com.mibombay.empresa.model.ProductoConReceta;
import com.mibombay.empresa.model.Receta;
import com.mibombay.empresa.repository.ProductoConRecetaRepository;
import com.mibombay.empresa.repository.RecetaRepository;
import com.mibombay.empresa.util.ValidacionDatos;

@Service
public class ProductoConRecetaService {

	private static final Logger log = LoggerFactory.getLogger(ProductoConRecetaService.class);

	private final ProductoConRecetaRepository productoRepository;
	private final RecetaRepository recetaRepository;
	private final ProductoConRecetaMapper productoMapper;

	public ProductoConRecetaService(ProductoConRecetaRepository productoRepository,
			RecetaRepository recetaRepository, ProductoConRecetaMapper productoMapper) {
		this.productoRepository = productoRepository;
		this.recetaRepository = recetaRepository;
		this.productoMapper = productoMapper;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public List<ProductoConRecetaDTO> listar() {
		log.debug("Listando productos con receta");
		List<ProductoConRecetaDTO> productos = productoMapper
				.toDTOList(productoRepository.findAll(Sort.by("nombre")));
		log.debug("Productos con receta encontrados: {}", productos.size());
		return productos;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProductoConRecetaDTO obtenerPorId(Long id) {
		log.debug("Obteniendo producto con receta id={}", id);
		return productoMapper.toDTO(obtenerEntidad(id));
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProductoConRecetaDTO crear(ProductoConRecetaDTO dto) {
		log.debug("Creando producto con receta nombre={} recetaId={}", dto.getNombre(), dto.getRecetaId());
		String nombre = ValidacionDatos.nombre(dto.getNombre());
		Receta receta = obtenerReceta(dto.getRecetaId());
		BigDecimal precioVenta = ValidacionDatos.stock(dto.getPrecioVenta(), "El precio de venta");
		validarPrecio(receta, precioVenta);

		if (productoRepository.existsByNombreIgnoreCase(nombre)) {
			log.debug("Alta rechazada, nombre duplicado: {}", nombre);
			throw new IllegalArgumentException("Ya existe un producto con ese nombre");
		}
		if (productoRepository.existsByRecetaId(receta.getId())) {
			log.debug("Alta rechazada, receta ya asignada id={}", receta.getId());
			throw new IllegalArgumentException("La receta ya está asignada a otro producto");
		}

		ProductoConReceta producto = productoMapper.toEntity(dto);
		producto.setNombre(nombre);
		producto.setReceta(receta);
		producto.setPrecioVenta(precioVenta);
		producto.setAdmiteAdicionales(dto.isAdmiteAdicionales());
		producto.setActivo(true);
		receta.setEnUso(true);
		recetaRepository.save(receta);
		ProductoConRecetaDTO respuesta = productoMapper.toDTO(productoRepository.save(producto));
		log.info("Producto con receta creado: id={} nombre={} receta={}", respuesta.getId(),
				respuesta.getNombre(), receta.getNombre());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProductoConRecetaDTO actualizar(Long id, ProductoConRecetaDTO dto) {
		log.debug("Actualizando producto con receta id={}", id);
		String nombre = ValidacionDatos.nombre(dto.getNombre());
		Receta receta = obtenerReceta(dto.getRecetaId());
		BigDecimal precioVenta = ValidacionDatos.stock(dto.getPrecioVenta(), "El precio de venta");
		validarPrecio(receta, precioVenta);

		ProductoConReceta producto = obtenerEntidad(id);
		if (!producto.getNombre().equalsIgnoreCase(nombre)
				&& productoRepository.existsByNombreIgnoreCase(nombre)) {
			log.debug("Actualización rechazada, nombre duplicado: {}", nombre);
			throw new IllegalArgumentException("Ya existe un producto con ese nombre");
		}
		Receta recetaAnterior = producto.getReceta();
		boolean cambiaReceta = !recetaAnterior.getId().equals(receta.getId());
		if (cambiaReceta && productoRepository.existsByRecetaId(receta.getId())) {
			log.debug("Actualización rechazada, receta ya asignada id={}", receta.getId());
			throw new IllegalArgumentException("La receta ya está asignada a otro producto");
		}

		producto.setNombre(nombre);
		producto.setReceta(receta);
		producto.setPrecioVenta(precioVenta);
		producto.setAdmiteAdicionales(dto.isAdmiteAdicionales());
		producto.setActivo(dto.isActivo());
		if (cambiaReceta) {
			recetaAnterior.setEnUso(false);
			receta.setEnUso(true);
			recetaRepository.save(recetaAnterior);
			recetaRepository.save(receta);
		}
		ProductoConRecetaDTO respuesta = productoMapper.toDTO(productoRepository.save(producto));
		log.info("Producto con receta actualizado: id={} nombre={} activo={}", respuesta.getId(),
				respuesta.getNombre(), respuesta.isActivo());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProductoConRecetaDTO alternarActivo(Long id) {
		log.debug("Cambiando estado producto con receta id={}", id);
		ProductoConReceta producto = obtenerEntidad(id);
		producto.setActivo(!producto.isActivo());
		ProductoConRecetaDTO respuesta = productoMapper.toDTO(productoRepository.save(producto));
		log.info("Estado cambiado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		return respuesta;
	}

	private ProductoConReceta obtenerEntidad(Long id) {
		return productoRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + id));
	}

	private Receta obtenerReceta(Long recetaId) {
		ValidacionDatos.requerido(recetaId, "La receta");
		return recetaRepository.findById(recetaId)
				.orElseThrow(() -> new NoSuchElementException("Receta no encontrada: " + recetaId));
	}

	private void validarPrecio(Receta receta, BigDecimal precioVenta) {
		if (receta.getCosto() != null && precioVenta.compareTo(receta.getCosto()) < 0) {
			log.debug("Operación rechazada, venta menor que costo de receta");
			throw new IllegalArgumentException("El precio de venta no puede ser menor al costo de la receta");
		}
	}

}
