package com.mibombay.empresa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.CompraDetalle;

public interface CompraDetalleRepository extends JpaRepository<CompraDetalle, Long> {

	List<CompraDetalle> findByCompraId(Long compraId);

	boolean existsByCompraIdAndIngredienteId(Long compraId, Long ingredienteId);

	boolean existsByCompraIdAndProductoId(Long compraId, Long productoId);

}
