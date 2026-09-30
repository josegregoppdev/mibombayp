package com.mibombay.empresa.dto;

import java.math.BigDecimal;

import com.mibombay.empresa.model.UnidadMedida;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DetalleRecetaDTO {

	private Long id;

	@NotNull(message = "La receta: no puede estar vacía")
	private Long recetaId;

	@NotNull(message = "El ingrediente: no puede estar vacío")
	private Long ingredienteId;

	private String ingredienteNombre;

	@NotNull(message = "La cantidad: no puede estar vacía")
	@DecimalMin(value = "0.00", message = "La cantidad: no puede ser negativa")
	private BigDecimal cantidad;

	@NotNull(message = "La unidad de medida: no puede estar vacía")
	private UnidadMedida unidadMedida;

}
