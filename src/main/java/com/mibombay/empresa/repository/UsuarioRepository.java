package com.mibombay.empresa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mibombay.empresa.model.Rol;
import com.mibombay.empresa.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByUsername(String username);

	boolean existsByUsername(String username);

	long countByActivoTrueAndRol(Rol rol);

}
