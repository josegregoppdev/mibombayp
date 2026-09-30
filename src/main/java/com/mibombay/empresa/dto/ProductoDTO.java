package com.mibombay.empresa.dto;

import java.math.BigDecimal;

import com.mibombay.empresa.model.Categoria;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductoDTO {

	private Long id;

	@NotBlank(message = "El nombre: no puede estar vacío")
	@Size(max = 100, message = "El nombre: máximo 100 caracteres")
	private String nombre;

	@NotNull(message = "La categoría: no puede estar vacía")
	private Categoria categoria;

	@NotNull(message = "El stock actual: no puede estar vacío")
	@DecimalMin(value = "0.00", message = "El stock actual: no puede ser negativo")
	private BigDecimal stockActual;

	@NotNull(message = "El stock mínimo: no puede estar vacío")
	@DecimalMin(value = "0.00", message = "El stock mínimo: no puede ser negativo")
	private BigDecimal stockMinimo;

	@NotNull(message = "El costo: no puede estar vacío")
	@DecimalMin(value = "0.00", message = "El costo: no puede ser negativo")
	private BigDecimal costo;

	@NotNull(message = "El precio de venta: no puede estar vacío")
	@DecimalMin(value = "0.00", message = "El precio de venta: no puede ser negativo")
	private BigDecimal precioVenta;

	private boolean activo;

}
