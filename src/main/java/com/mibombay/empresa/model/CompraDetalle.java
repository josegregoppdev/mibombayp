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
@Table(name = "compra_detalle", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "compra_id", "ingrediente_id" }),
		@UniqueConstraint(columnNames = { "compra_id", "producto_id" }) })
@Getter
@Setter
@NoArgsConstructor
public class CompraDetalle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "compra_id", nullable = false)
	private Compra compra;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private TipoItem tipo;

	@ManyToOne
	@JoinColumn(name = "ingrediente_id")
	private Ingrediente ingrediente;

	@ManyToOne
	@JoinColumn(name = "producto_id")
	private Producto producto;

	@Column(nullable = false, precision = 12, scale = 3)
	private BigDecimal cantidad = BigDecimal.ZERO;

	@Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
	private BigDecimal precioUnitario = BigDecimal.ZERO;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

}
