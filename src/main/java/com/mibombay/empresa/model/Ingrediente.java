package com.mibombay.empresa.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingrediente")
@Getter
@Setter
@NoArgsConstructor
public class Ingrediente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String nombre;

	@Enumerated(EnumType.STRING)
	@Column(name = "unidad_medida", nullable = false, length = 10)
	private UnidadMedida unidadMedida;

	@Column(name = "stock_actual", nullable = false, precision = 12, scale = 3)
	private BigDecimal stockActual = BigDecimal.ZERO;

	@Column(name = "stock_minimo", nullable = false, precision = 12, scale = 3)
	private BigDecimal stockMinimo = BigDecimal.ZERO;

	@Column(name = "valor_compra", nullable = false, precision = 12, scale = 2)
	private BigDecimal valorCompra = BigDecimal.ZERO;

	@Column(name = "valor_venta", nullable = false, precision = 12, scale = 2)
	private BigDecimal valorVenta = BigDecimal.ZERO;

	@Column(name = "es_adicional", nullable = false)
	private boolean esAdicional = true;

	@Column(nullable = false)
	private boolean activo = true;

}
