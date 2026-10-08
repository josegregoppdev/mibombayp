package com.mibombay.empresa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.Receta;

public interface RecetaRepository extends JpaRepository<Receta, Long> {

	boolean existsByNombreIgnoreCase(String nombre);

	Optional<Receta> findByNombreIgnoreCase(String nombre);

}
