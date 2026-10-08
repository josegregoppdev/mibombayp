package com.mibombay.empresa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

	boolean existsByDniNitIgnoreCase(String dniNit);

	Optional<Proveedor> findByDniNitIgnoreCase(String dniNit);

}
