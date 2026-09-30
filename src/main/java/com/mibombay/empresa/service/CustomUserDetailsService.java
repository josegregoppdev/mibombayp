package com.mibombay.empresa.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mibombay.empresa.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

	private final UsuarioRepository usuarioRepository;

	public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.debug("Autenticando username={}", username);
		return usuarioRepository.findByUsername(username)
				.filter(u -> u.isActivo())
				.map(u -> User.withUsername(u.getUsername())
						.password(u.getPassword())
						.authorities(u.getRol().getPrefijo())
						.build())
				.orElseThrow(() -> {
					log.debug("Autenticación fallida username={}", username);
					return new UsernameNotFoundException("Usuario no encontrado: " + username);
				});
	}

}
