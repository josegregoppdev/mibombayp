package com.mibombay.empresa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(length = 100)
	private String apellido;

	@Column(name = "dni_nit", nullable = false, unique = true, length = 15)
	private String dniNit;

	@Column(length = 100)
	private String direccion;

	@Column(length = 15)
	private String telefono;

	@Column(length = 100)
	private String email;

	@Column(nullable = false)
	private boolean activo = true;

}
