package com.mibombay.empresa.dto;

import com.mibombay.empresa.model.Rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioDTORequest {

	private Long id;

	@NotBlank(message = "El usuario: no puede estar vacío")
	@Size(max = 50, message = "El usuario: máximo 50 caracteres")
	@Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "El usuario: solo letras, números, punto, guion y guion bajo")
	private String username;

	@Pattern(regexp = "^$|.{4,}", message = "La contraseña: mínimo 4 caracteres")
	private String password;

	@NotBlank(message = "El nombre: no puede estar vacío")
	@Size(max = 100, message = "El nombre: máximo 100 caracteres")
	private String nombreCompleto;

	@NotNull(message = "El rol: no puede estar vacío")
	private Rol rol;

	private boolean activo;

}
