package com.mibombay.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.ProductoConReceta;

public interface ProductoConRecetaRepository extends JpaRepository<ProductoConReceta, Long> {

	boolean existsByNombreIgnoreCase(String nombre);

	boolean existsByRecetaId(Long recetaId);

}
