package com.mibombay.empresa.dto;

import com.mibombay.empresa.model.Rol;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UsuarioDTOResponse {

	private Long id;
	private String username;
	private String nombreCompleto;
	private Rol rol;
	private boolean activo;

}
