package com.mibombay.empresa.service;

import java.math.BigDecimal;
import java.util.List;

import com.mibombay.empresa.dto.ProductoDTO;
import com.mibombay.empresa.model.Categoria;
import com.mibombay.empresa.model.Producto;

/**
 * Fábrica de datos falsos para los tests de ProductoService.
 * Solo métodos estáticos: se llama como DataProviderProducto.productoValido(), etc.
 */
public final class DataProviderProducto {

	private DataProviderProducto() {
	}

	// Producto base válido (activo, con stock y precio >= costo)
	public static Producto productoValido() {
		Producto producto = new Producto();
		producto.setId(1L);
		producto.setNombre("Coca-Cola 400 ml");
		producto.setCategoria(Categoria.BEBIDAS);
		producto.setStockActual(new BigDecimal("48"));
		producto.setStockMinimo(new BigDecimal("10"));
		producto.setCosto(new BigDecimal("1800"));
		producto.setPrecioVenta(new BigDecimal("2000"));
		producto.setActivo(true);
		return producto;
	}

	// Producto inactivo (para probar la regla de ingresarStock)
	public static Producto productoInactivo() {
		Producto producto = productoValido();
		producto.setActivo(false);
		return producto;
	}

	// Lista de 3 productos variados (BEBIDAS, CERVEZAS, POSTRES) para listar()
	public static List<Producto> listaProductos() {
		Producto gaseosa = productoValido();

		Producto cerveza = productoValido();
		cerveza.setId(2L);
		cerveza.setNombre("Pilsen 330 ml");
		cerveza.setCategoria(Categoria.CERVEZAS);
		cerveza.setStockActual(new BigDecimal("24"));
		cerveza.setCosto(new BigDecimal("2200"));
		cerveza.setPrecioVenta(new BigDecimal("3000"));

		Producto postre = productoValido();
		postre.setId(3L);
		postre.setNombre("Brownie");
		postre.setCategoria(Categoria.POSTRES);
		postre.setStockActual(new BigDecimal("8"));
		postre.setCosto(new BigDecimal("2500"));
		postre.setPrecioVenta(new BigDecimal("4000"));

		return List.of(gaseosa, cerveza, postre);
	}

	// DTO completo y válido (base para los tests de crear/actualizar)
	public static ProductoDTO dtoValido() {
		ProductoDTO dto = new ProductoDTO();
		dto.setNombre("Coca-Cola 400 ml");
		dto.setCategoria(Categoria.BEBIDAS);
		dto.setStockActual(new BigDecimal("48"));
		dto.setStockMinimo(new BigDecimal("10"));
		dto.setCosto(new BigDecimal("1800"));
		dto.setPrecioVenta(new BigDecimal("2000"));
		dto.setActivo(true);
		return dto;
	}

	// DTO con activo=false (para probar que actualizar aplica el flag del DTO)
	public static ProductoDTO dtoDesactivado() {
		ProductoDTO dto = dtoValido();
		dto.setActivo(false);
		return dto;
	}

	// DTO con precio de venta por debajo del costo (rompe validarMargen)
	public static ProductoDTO dtoPrecioBajoCosto() {
		ProductoDTO dto = dtoValido();
		dto.setPrecioVenta(new BigDecimal("1500"));
		return dto;
	}

}
