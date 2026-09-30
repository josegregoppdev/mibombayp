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

import com.mibombay.empresa.dto.ProductoDTO;
import com.mibombay.empresa.mapper.ProductoMapper;
import com.mibombay.empresa.model.Categoria;
import com.mibombay.empresa.model.Producto;
import com.mibombay.empresa.repository.ProductoRepository;
import com.mibombay.empresa.util.ValidacionDatos;

@Service
public class ProductoService {

	private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

	private final ProductoRepository productoRepository;
	private final ProductoMapper productoMapper;

	public ProductoService(ProductoRepository productoRepository, ProductoMapper productoMapper) {
		this.productoRepository = productoRepository;
		this.productoMapper = productoMapper;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public List<ProductoDTO> listar() {
		log.debug("Listando productos");
		List<ProductoDTO> productos = productoMapper
				.toDTOList(productoRepository.findAll(Sort.by("nombre")));
		log.debug("Productos encontrados: {}", productos.size());
		return productos;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProductoDTO obtenerPorId(Long id) {
		log.debug("Obteniendo producto id={}", id);
		return productoMapper.toDTO(obtenerEntidad(id));
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProductoDTO crear(ProductoDTO dto) {
		log.debug("Creando producto nombre={} categoria={}", dto.getNombre(), dto.getCategoria());
		String nombre = ValidacionDatos.nombre(dto.getNombre());
		Categoria categoria = dto.getCategoria();
		ValidacionDatos.noNulo(categoria, "La categoría");
		BigDecimal stockActual = ValidacionDatos.stock(dto.getStockActual(), "El stock actual");
		BigDecimal stockMinimo = ValidacionDatos.stock(dto.getStockMinimo(), "El stock mínimo");
		BigDecimal costo = ValidacionDatos.stock(dto.getCosto(), "El costo");
		BigDecimal precioVenta = ValidacionDatos.stock(dto.getPrecioVenta(), "El precio de venta");
		validarMargen(costo, precioVenta);

		if (productoRepository.existsByNombreIgnoreCase(nombre)) {
			log.debug("Alta rechazada, nombre duplicado: {}", nombre);
			throw new IllegalArgumentException("Ya existe un producto con ese nombre");
		}

		Producto producto = productoMapper.toEntity(dto);
		producto.setNombre(nombre);
		producto.setCategoria(categoria);
		producto.setStockActual(stockActual);
		producto.setStockMinimo(stockMinimo);
		producto.setCosto(costo);
		producto.setPrecioVenta(precioVenta);
		producto.setActivo(true);
		ProductoDTO respuesta = productoMapper.toDTO(productoRepository.save(producto));
		log.info("Producto creado: id={} nombre={} categoria={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.getCategoria());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProductoDTO actualizar(Long id, ProductoDTO dto) {
		log.debug("Actualizando producto id={}", id);
		String nombre = ValidacionDatos.nombre(dto.getNombre());
		Categoria categoria = dto.getCategoria();
		ValidacionDatos.noNulo(categoria, "La categoría");
		BigDecimal stockActual = ValidacionDatos.stock(dto.getStockActual(), "El stock actual");
		BigDecimal stockMinimo = ValidacionDatos.stock(dto.getStockMinimo(), "El stock mínimo");
		BigDecimal costo = ValidacionDatos.stock(dto.getCosto(), "El costo");
		BigDecimal precioVenta = ValidacionDatos.stock(dto.getPrecioVenta(), "El precio de venta");
		validarMargen(costo, precioVenta);

		Producto producto = obtenerEntidad(id);
		if (!producto.getNombre().equalsIgnoreCase(nombre)
				&& productoRepository.existsByNombreIgnoreCase(nombre)) {
			log.debug("Actualización rechazada, nombre duplicado: {}", nombre);
			throw new IllegalArgumentException("Ya existe un producto con ese nombre");
		}

		producto.setNombre(nombre);
		producto.setCategoria(categoria);
		producto.setStockActual(stockActual);
		producto.setStockMinimo(stockMinimo);
		producto.setCosto(costo);
		producto.setPrecioVenta(precioVenta);
		producto.setActivo(dto.isActivo());
		ProductoDTO respuesta = productoMapper.toDTO(productoRepository.save(producto));
		log.info("Producto actualizado: id={} nombre={} categoria={} activo={}", respuesta.getId(),
				respuesta.getNombre(), respuesta.getCategoria(), respuesta.isActivo());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public ProductoDTO alternarActivo(Long id) {
		log.debug("Cambiando estado producto id={}", id);
		Producto producto = obtenerEntidad(id);
		producto.setActivo(!producto.isActivo());
		ProductoDTO respuesta = productoMapper.toDTO(productoRepository.save(producto));
		log.info("Estado cambiado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		return respuesta;
	}

	private Producto obtenerEntidad(Long id) {
		return productoRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + id));
	}

	private void validarMargen(BigDecimal costo, BigDecimal precioVenta) {
		if (precioVenta.compareTo(costo) < 0) {
			log.debug("Operación rechazada, venta menor que costo");
			throw new IllegalArgumentException("El precio de venta no puede ser menor al costo");
		}
	}

}
