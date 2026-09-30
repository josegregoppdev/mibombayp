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

import com.mibombay.empresa.dto.IngredienteDTO;
import com.mibombay.empresa.model.UnidadMedida;
import com.mibombay.empresa.service.IngredienteService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/ingredientes")
@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
public class IngredienteAdminController {

	private static final Logger log = LoggerFactory.getLogger(IngredienteAdminController.class);

	private final IngredienteService ingredienteService;

	public IngredienteAdminController(IngredienteService ingredienteService) {
		this.ingredienteService = ingredienteService;
	}

	@GetMapping
	public String listar(Model model) {
		log.debug("GET /admin/ingredientes");
		model.addAttribute("ingredientes", ingredienteService.listar());
		return "admin/ingredientes/ingredientes";
	}

	@GetMapping("/nuevo")
	public String nuevo(Model model) {
		log.debug("GET /admin/ingredientes/nuevo");
		model.addAttribute("ingrediente", new IngredienteDTO());
		model.addAttribute("unidades", UnidadMedida.values());
		model.addAttribute("edicion", false);
		return "admin/ingredientes/ingrediente-form";
	}

	@PostMapping
	public String crear(@Valid @ModelAttribute("ingrediente") IngredienteDTO ingrediente,
			BindingResult resultado, Model model, RedirectAttributes ra) {
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en alta: {} errores", resultado.getErrorCount());
			model.addAttribute("unidades", UnidadMedida.values());
			model.addAttribute("edicion", false);
			return "admin/ingredientes/ingrediente-form";
		}
		IngredienteDTO respuesta = ingredienteService.crear(ingrediente);
		log.info("Alta ingrediente: id={} nombre={} unidad={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.getUnidadMedida());
		ra.addFlashAttribute("exito", "Ingrediente creado correctamente.");
		return "redirect:/admin/ingredientes";
	}

	@GetMapping("/{id}/editar")
	public String editar(@PathVariable Long id, Model model) {
		log.debug("GET /admin/ingredientes/{}/editar", id);
		model.addAttribute("ingrediente", ingredienteService.obtenerPorId(id));
		model.addAttribute("unidades", UnidadMedida.values());
		model.addAttribute("edicion", true);
		return "admin/ingredientes/ingrediente-form";
	}

	@PostMapping("/{id}")
	public String actualizar(@PathVariable Long id,
			@Valid @ModelAttribute("ingrediente") IngredienteDTO ingrediente, BindingResult resultado,
			Model model, RedirectAttributes ra) {
		ingrediente.setId(id);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en edición id={}: {} errores", id, resultado.getErrorCount());
			model.addAttribute("unidades", UnidadMedida.values());
			model.addAttribute("edicion", true);
			return "admin/ingredientes/ingrediente-form";
		}
		IngredienteDTO respuesta = ingredienteService.actualizar(id, ingrediente);
		log.info("Edición ingrediente: id={} nombre={} unidad={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.getUnidadMedida());
		ra.addFlashAttribute("exito", "Ingrediente actualizado correctamente.");
		return "redirect:/admin/ingredientes";
	}

	@PostMapping("/{id}/activo")
	public String alternarActivo(@PathVariable Long id, RedirectAttributes ra) {
		IngredienteDTO respuesta = ingredienteService.alternarActivo(id);
		log.info("Cambio estado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		ra.addFlashAttribute("exito", "Estado del ingrediente actualizado correctamente.");
		return "redirect:/admin/ingredientes";
	}

}
