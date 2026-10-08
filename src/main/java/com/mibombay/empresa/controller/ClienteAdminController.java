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

import com.mibombay.empresa.dto.ClienteDTO;
import com.mibombay.empresa.service.ClienteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/clientes")
@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
public class ClienteAdminController {

	private static final Logger log = LoggerFactory.getLogger(ClienteAdminController.class);

	private final ClienteService clienteService;

	public ClienteAdminController(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	@GetMapping
	public String listar(Model model) {
		log.debug("GET /admin/clientes");
		model.addAttribute("clientes", clienteService.listar());
		return "admin/clientes/clientes";
	}

	@GetMapping("/nuevo")
	public String nuevo(Model model) {
		log.debug("GET /admin/clientes/nuevo");
		model.addAttribute("cliente", new ClienteDTO());
		model.addAttribute("edicion", false);
		return "admin/clientes/cliente-form";
	}

	@PostMapping
	public String crear(@Valid @ModelAttribute("cliente") ClienteDTO cliente,
			BindingResult resultado, Model model, RedirectAttributes ra) {
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en alta: {} errores", resultado.getErrorCount());
			model.addAttribute("edicion", false);
			return "admin/clientes/cliente-form";
		}
		ClienteDTO respuesta = clienteService.crear(cliente);
		log.info("Alta cliente: id={} nombre={} dniNit={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.getDniNit());
		ra.addFlashAttribute("exito", "Cliente creado correctamente.");
		return "redirect:/admin/clientes";
	}

	@GetMapping("/{id}/editar")
	public String editar(@PathVariable Long id, Model model) {
		log.debug("GET /admin/clientes/{}/editar", id);
		model.addAttribute("cliente", clienteService.obtenerPorId(id));
		model.addAttribute("edicion", true);
		return "admin/clientes/cliente-form";
	}

	@PostMapping("/{id}")
	public String actualizar(@PathVariable Long id,
			@Valid @ModelAttribute("cliente") ClienteDTO cliente, BindingResult resultado,
			Model model, RedirectAttributes ra) {
		cliente.setId(id);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en edición id={}: {} errores", id, resultado.getErrorCount());
			model.addAttribute("edicion", true);
			return "admin/clientes/cliente-form";
		}
		ClienteDTO respuesta = clienteService.actualizar(id, cliente);
		log.info("Edición cliente: id={} nombre={}", respuesta.getId(), respuesta.getNombre());
		ra.addFlashAttribute("exito", "Cliente actualizado correctamente.");
		return "redirect:/admin/clientes";
	}

	@PostMapping("/{id}/activo")
	public String alternarActivo(@PathVariable Long id, RedirectAttributes ra) {
		ClienteDTO respuesta = clienteService.alternarActivo(id);
		log.info("Cambio estado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		ra.addFlashAttribute("exito", "Estado del cliente actualizado correctamente.");
		return "redirect:/admin/clientes";
	}

}
