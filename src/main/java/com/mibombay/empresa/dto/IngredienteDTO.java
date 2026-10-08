package com.mibombay.empresa.dto;

import java.math.BigDecimal;

import com.mibombay.empresa.model.UnidadMedida;

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
public class IngredienteDTO {

	private Long id;

	@NotBlank(message = "El nombre: no puede estar vacío")
	@Size(max = 100, message = "El nombre: máximo 100 caracteres")
	private String nombre;

	@NotNull(message = "La unidad de medida: no puede estar vacía")
	private UnidadMedida unidadMedida;

	@NotNull(message = "El stock actual: no puede estar vacío")
	@DecimalMin(value = "0.00", message = "El stock actual: no puede ser negativo")
	private BigDecimal stockActual;

	@NotNull(message = "El stock mínimo: no puede estar vacío")
	@DecimalMin(value = "0.00", message = "El stock mínimo: no puede ser negativo")
	private BigDecimal stockMinimo;

	@NotNull(message = "El valor de compra: no puede estar vacío")
	@DecimalMin(value = "0.00", message = "El valor de compra: no puede ser negativo")
	private BigDecimal valorCompra;

	@NotNull(message = "La porción adicional: no puede estar vacía")
	@DecimalMin(value = "0.00", message = "La porción adicional: no puede ser negativa")
	private BigDecimal porcionAdicional = BigDecimal.ZERO;

	@NotNull(message = "El precio adicional: no puede estar vacío")
	@DecimalMin(value = "0.00", message = "El precio adicional: no puede ser negativo")
	private BigDecimal precioAdicional = BigDecimal.ZERO;

	private boolean esAdicional = true;

	private boolean activo;

}
