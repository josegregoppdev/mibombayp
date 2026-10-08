package com.mibombay.empresa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/login", "/css/**", "/js/**", "/error").permitAll()
				.requestMatchers("/admin/usuarios", "/admin/usuarios/**").hasAnyRole("DEV", "ADMIN")
				.requestMatchers("/admin/ingredientes", "/admin/ingredientes/**").hasAnyRole("DEV", "ADMIN")
				.requestMatchers("/admin/productos", "/admin/productos/**").hasAnyRole("DEV", "ADMIN")
				.requestMatchers("/admin/recetas", "/admin/recetas/**").hasAnyRole("DEV", "ADMIN")
				.requestMatchers("/admin/productos-con-receta", "/admin/productos-con-receta/**").hasAnyRole("DEV", "ADMIN")
				.requestMatchers("/admin/clientes", "/admin/clientes/**").hasAnyRole("DEV", "ADMIN")
				.requestMatchers("/admin/proveedores", "/admin/proveedores/**").hasAnyRole("DEV", "ADMIN")

				.requestMatchers("/admin/compras", "/admin/compras/**").hasAnyRole("DEV", "ADMIN")
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/venta/**").hasAnyRole("ADMIN", "CAJERO")
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.defaultSuccessUrl("/", true)
				.failureUrl("/login?error")
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout")
				.permitAll()
			);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
