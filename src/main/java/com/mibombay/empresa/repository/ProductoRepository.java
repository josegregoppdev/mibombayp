package com.mibombay.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

	boolean existsByNombreIgnoreCase(String nombre);

}
