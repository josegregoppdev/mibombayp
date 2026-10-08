package com.mibombay.empresa.dto;

import java.math.BigDecimal;

import com.mibombay.empresa.model.TipoItem;
import com.mibombay.empresa.model.UnidadMedida;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompraDetalleDTO {

	private Long id;

	private Long compraId;

	// Lo fija el controlador según el form (ingrediente/producto); sin @NotNull en el DTO
	private TipoItem tipo;

	private Long ingredienteId;

	private Long productoId;

	private String ingredienteNombre;

	private String productoNombre;

	// La deriva el mapper desde el ingrediente; no viaja en el form
	private UnidadMedida unidadMedida;

	@NotNull(message = "La cantidad: no puede estar vacía")
	@DecimalMin(value = "0.00", message = "La cantidad: no puede ser negativa")
	private BigDecimal cantidad;

	@NotNull(message = "El precio de compra: no puede estar vacío")
	@DecimalMin(value = "0.00", message = "El precio de compra: no puede ser negativo")
	private BigDecimal precioUnitario;

	// Lo calcula CompraService; no viaja en el form
	private BigDecimal subtotal;

}
