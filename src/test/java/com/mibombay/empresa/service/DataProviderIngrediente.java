package com.mibombay.empresa.service;

import java.math.BigDecimal;
import java.util.List;

import com.mibombay.empresa.dto.IngredienteDTO;
import com.mibombay.empresa.model.Ingrediente;
import com.mibombay.empresa.model.UnidadMedida;

/**
 * Fábrica de datos falsos para los tests de IngredienteService.
 * Solo métodos estáticos: se llama como DataProviderIngrediente.ingredienteValido(), etc.
 */
public final class DataProviderIngrediente {

	private DataProviderIngrediente() {
	}

	// Ingrediente base válido (activo, con stock y adicional configurado)
	public static Ingrediente ingredienteValido() {
		Ingrediente ingrediente = new Ingrediente();
		ingrediente.setId(1L);
		ingrediente.setNombre("Pan perro");
		ingrediente.setUnidadMedida(UnidadMedida.UD);
		ingrediente.setStockActual(new BigDecimal("100"));
		ingrediente.setStockMinimo(new BigDecimal("5"));
		ingrediente.setValorCompra(new BigDecimal("800"));
		ingrediente.setPorcionAdicional(new BigDecimal("0.02"));
		ingrediente.setPrecioAdicional(new BigDecimal("800"));
		ingrediente.setEsAdicional(true);
		ingrediente.setActivo(true);
		return ingrediente;
	}

	// Ingrediente inactivo (para probar la regla de ingresarStock)
	public static Ingrediente ingredienteInactivo() {
		Ingrediente ingrediente = ingredienteValido();
		ingrediente.setActivo(false);
		return ingrediente;
	}

	// Lista de 3 ingredientes variados para listar()
	public static List<Ingrediente> listaIngredientes() {
		Ingrediente pan = ingredienteValido();

		Ingrediente salchicha = ingredienteValido();
		salchicha.setId(2L);
		salchicha.setNombre("Salchicha");
		salchicha.setUnidadMedida(UnidadMedida.KG);
		salchicha.setStockActual(new BigDecimal("30"));
		salchicha.setValorCompra(new BigDecimal("15000"));

		Ingrediente mayonesa = ingredienteValido();
		mayonesa.setId(3L);
		mayonesa.setNombre("Mayonesa");
		mayonesa.setUnidadMedida(UnidadMedida.L);
		mayonesa.setStockActual(new BigDecimal("12.5"));
		mayonesa.setValorCompra(new BigDecimal("9000"));
		mayonesa.setEsAdicional(true);
		mayonesa.setPorcionAdicional(new BigDecimal("0.02"));
		mayonesa.setPrecioAdicional(new BigDecimal("800"));

		return List.of(pan, salchicha, mayonesa);
	}

	// DTO completo y válido (base para los tests de crear/actualizar)
	public static IngredienteDTO dtoValido() {
		IngredienteDTO dto = new IngredienteDTO();
		dto.setNombre("Pan perro");
		dto.setUnidadMedida(UnidadMedida.UD);
		dto.setStockActual(new BigDecimal("100"));
		dto.setStockMinimo(new BigDecimal("5"));
		dto.setValorCompra(new BigDecimal("800"));
		dto.setPorcionAdicional(new BigDecimal("0.02"));
		dto.setPrecioAdicional(new BigDecimal("800"));
		dto.setEsAdicional(true);
		dto.setActivo(true);
		return dto;
	}

	// DTO con activo=false (para probar que actualizar aplica el flag del DTO)
	public static IngredienteDTO dtoDesactivado() {
		IngredienteDTO dto = dtoValido();
		dto.setActivo(false);
		return dto;
	}

}
