package com.mibombay.empresa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.DetalleReceta;

public interface DetalleRecetaRepository extends JpaRepository<DetalleReceta, Long> {

	List<DetalleReceta> findByRecetaId(Long recetaId);

	List<DetalleReceta> findByIngredienteId(Long ingredienteId);

	boolean existsByRecetaIdAndIngredienteId(Long recetaId, Long ingredienteId);

}
