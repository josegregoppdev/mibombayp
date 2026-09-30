package com.mibombay.empresa.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_receta", uniqueConstraints = @UniqueConstraint(columnNames = { "receta_id",
		"ingrediente_id" }))
@Getter
@Setter
@NoArgsConstructor
public class DetalleReceta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "receta_id", nullable = false)
	private Receta receta;

	@ManyToOne(optional = false)
	@JoinColumn(name = "ingrediente_id", nullable = false)
	private Ingrediente ingrediente;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal cantidad = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(name = "unidad_medida", nullable = false, length = 10)
	private UnidadMedida unidadMedida;

}
