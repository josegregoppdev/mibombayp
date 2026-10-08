package com.mibombay.empresa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.Compra;

public interface CompraRepository extends JpaRepository<Compra, Long> {

	List<Compra> findAllByOrderByFechaDesc();

}
