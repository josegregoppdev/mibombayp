package com.mibombay.empresa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClienteDTO {

	private Long id;

	@NotBlank(message = "El nombre: no puede estar vacío")
	@Size(max = 100, message = "El nombre: máximo 100 caracteres")
	private String nombre;

	@Size(max = 100, message = "El apellido: máximo 100 caracteres")
	private String apellido;

	@NotBlank(message = "El DNI/NIT: no puede estar vacío")
	@Size(max = 15, message = "El DNI/NIT: máximo 15 caracteres")
	private String dniNit;

	@Size(max = 100, message = "La dirección: máximo 100 caracteres")
	private String direccion;

	@Size(max = 15, message = "El teléfono: máximo 15 caracteres")
	private String telefono;

	@Email(message = "El email: no tiene un formato válido")
	@Size(max = 100, message = "El email: máximo 100 caracteres")
	private String email;

	private boolean activo;

}
