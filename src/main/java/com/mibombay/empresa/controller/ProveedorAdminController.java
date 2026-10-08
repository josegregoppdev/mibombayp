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

import com.mibombay.empresa.dto.ProveedorDTO;
import com.mibombay.empresa.service.ProveedorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/proveedores")
@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
public class ProveedorAdminController {

	private static final Logger log = LoggerFactory.getLogger(ProveedorAdminController.class);

	private final ProveedorService proveedorService;

	public ProveedorAdminController(ProveedorService proveedorService) {
		this.proveedorService = proveedorService;
	}

	@GetMapping
	public String listar(Model model) {
		log.debug("GET /admin/proveedores");
		model.addAttribute("proveedores", proveedorService.listar());
		return "admin/proveedores/proveedores";
	}

	@GetMapping("/nuevo")
	public String nuevo(Model model) {
		log.debug("GET /admin/proveedores/nuevo");
		model.addAttribute("proveedor", new ProveedorDTO());
		model.addAttribute("edicion", false);
		return "admin/proveedores/proveedor-form";
	}

	@PostMapping
	public String crear(@Valid @ModelAttribute("proveedor") ProveedorDTO proveedor,
			BindingResult resultado, Model model, RedirectAttributes ra) {
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en alta: {} errores", resultado.getErrorCount());
			model.addAttribute("edicion", false);
			return "admin/proveedores/proveedor-form";
		}
		ProveedorDTO respuesta = proveedorService.crear(proveedor);
		log.info("Alta proveedor: id={} nombre={} dniNit={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.getDniNit());
		ra.addFlashAttribute("exito", "Proveedor creado correctamente.");
		return "redirect:/admin/proveedores";
	}

	@GetMapping("/{id}/editar")
	public String editar(@PathVariable Long id, Model model) {
		log.debug("GET /admin/proveedores/{}/editar", id);
		model.addAttribute("proveedor", proveedorService.obtenerPorId(id));
		model.addAttribute("edicion", true);
		return "admin/proveedores/proveedor-form";
	}

	@PostMapping("/{id}")
	public String actualizar(@PathVariable Long id,
			@Valid @ModelAttribute("proveedor") ProveedorDTO proveedor, BindingResult resultado,
			Model model, RedirectAttributes ra) {
		proveedor.setId(id);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en edición id={}: {} errores", id, resultado.getErrorCount());
			model.addAttribute("edicion", true);
			return "admin/proveedores/proveedor-form";
		}
		ProveedorDTO respuesta = proveedorService.actualizar(id, proveedor);
		log.info("Edición proveedor: id={} nombre={}", respuesta.getId(), respuesta.getNombre());
		ra.addFlashAttribute("exito", "Proveedor actualizado correctamente.");
		return "redirect:/admin/proveedores";
	}

	@PostMapping("/{id}/activo")
	public String alternarActivo(@PathVariable Long id, RedirectAttributes ra) {
		ProveedorDTO respuesta = proveedorService.alternarActivo(id);
		log.info("Cambio estado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		ra.addFlashAttribute("exito", "Estado del proveedor actualizado correctamente.");
		return "redirect:/admin/proveedores";
	}

}
