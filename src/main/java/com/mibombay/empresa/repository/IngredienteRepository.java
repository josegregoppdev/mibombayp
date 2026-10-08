package com.mibombay.empresa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.Ingrediente;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

	boolean existsByNombreIgnoreCase(String nombre);

	Optional<Ingrediente> findByNombreIgnoreCase(String nombre);

}
