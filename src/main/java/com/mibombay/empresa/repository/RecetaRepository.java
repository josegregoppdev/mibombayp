package com.mibombay.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.Receta;

public interface RecetaRepository extends JpaRepository<Receta, Long> {

	boolean existsByNombreIgnoreCase(String nombre);

}
