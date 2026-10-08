package com.mibombay.empresa.dto;

import java.math.BigDecimal;

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
public class ProductoConRecetaDTO {

	private Long id;

	@NotBlank(message = "El nombre: no puede estar vacío")
	@Size(max = 100, message = "El nombre: máximo 100 caracteres")
	private String nombre;

	@NotNull(message = "La receta: no puede estar vacía")
	private Long recetaId;

	private String recetaNombre;

	private BigDecimal recetaCosto;

	@NotNull(message = "El precio de venta: no puede estar vacío")
	@DecimalMin(value = "0.00", message = "El precio de venta: no puede ser negativo")
	private BigDecimal precioVenta;

	private boolean activo;

}
