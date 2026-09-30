package com.mibombay.empresa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mibombay.empresa.dto.UsuarioDTORequest;
import com.mibombay.empresa.model.Rol;
import com.mibombay.empresa.repository.UsuarioRepository;
import com.mibombay.empresa.service.UsuarioService;

@Component
public class DataInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private final UsuarioRepository usuarioRepository;
	private final UsuarioService usuarioService;

	public DataInitializer(UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
		this.usuarioRepository = usuarioRepository;
		this.usuarioService = usuarioService;
	}

	@Override
	public void run(String... args) {
		crearSiNoExiste("admin", "admin123", "Administrador", Rol.ADMIN);
		crearSiNoExiste("cajero", "cajero123", "Cajero", Rol.CAJERO);
		crearSiNoExiste("dev", "dev123", "Desarrollador", Rol.DEV);
	}

	private void crearSiNoExiste(String username, String password, String nombreCompleto, Rol rol) {
		if (usuarioRepository.existsByUsername(username)) {
			return;
		}
		UsuarioDTORequest request = new UsuarioDTORequest();
		request.setUsername(username);
		request.setPassword(password);
		request.setNombreCompleto(nombreCompleto);
		request.setRol(rol);
		usuarioService.crearUsuario(request);
		log.info("Usuario semilla creado: {}", username);
	}

}
