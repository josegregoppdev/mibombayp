package com.mibombay.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	boolean existsByDniNitIgnoreCase(String dniNit);

}
