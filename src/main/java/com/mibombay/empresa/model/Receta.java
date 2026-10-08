package com.mibombay.empresa.model;

import java.math.BigDecimal;

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
@Table(name = "receta")
@Getter
@Setter
@NoArgsConstructor
public class Receta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String nombre;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal costo = BigDecimal.ZERO;

	@Column(name = "en_uso", nullable = false)
	private boolean enUso = false;

	@Column(nullable = false)
	private boolean activo = true;

}
