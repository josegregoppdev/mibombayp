package com.mibombay.empresa.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompraDTO {

	private Long id;

	// Fecha como texto ya formateada (dd/MM/yyyy HH:mm); solo lectura, no viaja en el form
	private String fecha;

	// Opcional en el form: vacío = proveedor por defecto (lo resuelve CompraService)
	private Long proveedorId;

	private String proveedorNombre;

	private String estado;

	private BigDecimal total;

	@Size(max = 255, message = "Las observaciones: máximo 255 caracteres")
	private String observaciones;

}
