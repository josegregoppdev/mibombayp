package com.mibombay.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.Ingrediente;

public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

	boolean existsByNombreIgnoreCase(String nombre);

}
