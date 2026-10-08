package com.mibombay.empresa.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecetaDTO {

	private Long id;

	@NotBlank(message = "El nombre: no puede estar vacío")
	@Size(max = 100, message = "El nombre: máximo 100 caracteres")
	private String nombre;

	private BigDecimal costo = BigDecimal.ZERO;

	private boolean enUso;

	private boolean activo;

}
