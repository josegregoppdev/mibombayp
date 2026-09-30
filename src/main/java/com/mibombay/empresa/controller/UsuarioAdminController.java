package com.mibombay.empresa.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mibombay.empresa.dto.UsuarioDTORequest;
import com.mibombay.empresa.dto.UsuarioDTOResponse;
import com.mibombay.empresa.mapper.UsuarioMapper;
import com.mibombay.empresa.model.Rol;
import com.mibombay.empresa.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
public class UsuarioAdminController {

	private static final Logger log = LoggerFactory.getLogger(UsuarioAdminController.class);

	private final UsuarioService usuarioService;
	private final UsuarioMapper usuarioMapper;

	public UsuarioAdminController(UsuarioService usuarioService, UsuarioMapper usuarioMapper) {
		this.usuarioService = usuarioService;
		this.usuarioMapper = usuarioMapper;
	}

	@GetMapping
	public String listar(Model model) {
		log.debug("GET /admin/usuarios");
		model.addAttribute("usuarios", usuarioService.listar());
		return "admin/usuarios/usuarios";
	}

	@GetMapping("/nuevo")
	public String nuevo(Model model) {
		log.debug("GET /admin/usuarios/nuevo");
		model.addAttribute("usuario", new UsuarioDTORequest());
		model.addAttribute("roles", Rol.values());
		model.addAttribute("edicion", false);
		return "admin/usuarios/usuario-form";
	}

	@PostMapping
	public String crear(@Valid @ModelAttribute("usuario") UsuarioDTORequest usuario, BindingResult resultado,
			Model model, RedirectAttributes ra) {
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en alta: {} errores", resultado.getErrorCount());
			model.addAttribute("roles", Rol.values());
			model.addAttribute("edicion", false);
			return "admin/usuarios/usuario-form";
		}
		UsuarioDTOResponse respuesta = usuarioService.crearUsuario(usuario);
		log.info("Alta usuario: id={} username={} rol={}", respuesta.getId(), respuesta.getUsername(),
				respuesta.getRol());
		ra.addFlashAttribute("exito", "Usuario creado correctamente.");
		return "redirect:/admin/usuarios";
	}

	@GetMapping("/{id}/editar")
	public String editar(@PathVariable Long id, Model model) {
		log.debug("GET /admin/usuarios/{}/editar", id);
		model.addAttribute("usuario", usuarioMapper.toRequest(usuarioService.obtenerPorId(id)));
		model.addAttribute("roles", Rol.values());
		model.addAttribute("edicion", true);
		return "admin/usuarios/usuario-form";
	}

	@PostMapping("/{id}")
	public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("usuario") UsuarioDTORequest usuario,
			BindingResult resultado, Model model, RedirectAttributes ra) {
		usuario.setId(id);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en edición id={}: {} errores", id, resultado.getErrorCount());
			model.addAttribute("roles", Rol.values());
			model.addAttribute("edicion", true);
			return "admin/usuarios/usuario-form";
		}
		UsuarioDTOResponse respuesta = usuarioService.actualizar(id, usuario);
		log.info("Edición usuario: id={} username={} rol={}", respuesta.getId(), respuesta.getUsername(),
				respuesta.getRol());
		ra.addFlashAttribute("exito", "Usuario actualizado correctamente.");
		return "redirect:/admin/usuarios";
	}

	@PostMapping("/{id}/activo")
	public String alternarActivo(@PathVariable Long id, RedirectAttributes ra) {
		UsuarioDTOResponse respuesta = usuarioService.alternarActivo(id);
		log.info("Cambio estado: id={} username={} rol={} activo={}", respuesta.getId(),
				respuesta.getUsername(), respuesta.getRol(), respuesta.isActivo());
		ra.addFlashAttribute("exito", "Estado del usuario actualizado correctamente.");
		return "redirect:/admin/usuarios";
	}

}
