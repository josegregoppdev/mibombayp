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

import com.mibombay.empresa.dto.DetalleRecetaDTO;
import com.mibombay.empresa.dto.RecetaDTO;
import com.mibombay.empresa.model.UnidadMedida;
import com.mibombay.empresa.service.IngredienteService;
import com.mibombay.empresa.service.RecetaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/recetas")
@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
public class RecetaAdminController {

	private static final Logger log = LoggerFactory.getLogger(RecetaAdminController.class);

	private final RecetaService recetaService;
	private final IngredienteService ingredienteService;

	public RecetaAdminController(RecetaService recetaService, IngredienteService ingredienteService) {
		this.recetaService = recetaService;
		this.ingredienteService = ingredienteService;
	}

	@GetMapping
	public String listar(Model model) {
		log.debug("GET /admin/recetas");
		model.addAttribute("recetas", recetaService.listar());
		return "admin/recetas/recetas";
	}

	@GetMapping("/nuevo")
	public String nuevo(Model model) {
		log.debug("GET /admin/recetas/nuevo");
		model.addAttribute("receta", new RecetaDTO());
		model.addAttribute("edicion", false);
		return "admin/recetas/receta-form";
	}

	@PostMapping
	public String crear(@Valid @ModelAttribute("receta") RecetaDTO receta,
			BindingResult resultado, Model model, RedirectAttributes ra) {
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en alta: {} errores", resultado.getErrorCount());
			model.addAttribute("edicion", false);
			return "admin/recetas/receta-form";
		}
		RecetaDTO respuesta = recetaService.crear(receta);
		log.info("Alta receta: id={} nombre={}", respuesta.getId(), respuesta.getNombre());
		ra.addFlashAttribute("exito", "Receta creada correctamente.");
		return "redirect:/admin/recetas/" + respuesta.getId() + "/editar";
	}

	@GetMapping("/{id}/editar")
	public String editar(@PathVariable Long id, Model model) {
		log.debug("GET /admin/recetas/{}/editar", id);
		model.addAttribute("receta", recetaService.obtenerPorId(id));
		cargarDetalle(model, id, newDetalle(id));
		model.addAttribute("edicion", true);
		return "admin/recetas/receta-form";
	}

	@PostMapping("/{id}")
	public String actualizar(@PathVariable Long id,
			@Valid @ModelAttribute("receta") RecetaDTO receta, BindingResult resultado,
			Model model, RedirectAttributes ra) {
		receta.setId(id);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en edición id={}: {} errores", id, resultado.getErrorCount());
			cargarDetalle(model, id, newDetalle(id));
			model.addAttribute("edicion", true);
			return "admin/recetas/receta-form";
		}
		RecetaDTO respuesta = recetaService.actualizar(id, receta);
		log.info("Edición receta: id={} nombre={}", respuesta.getId(), respuesta.getNombre());
		ra.addFlashAttribute("exito", "Receta actualizada correctamente.");
		return "redirect:/admin/recetas";
	}

	@PostMapping("/{id}/activo")
	public String alternarActivo(@PathVariable Long id, RedirectAttributes ra) {
		RecetaDTO respuesta = recetaService.alternarActivo(id);
		log.info("Cambio estado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		ra.addFlashAttribute("exito", "Estado de la receta actualizado correctamente.");
		return "redirect:/admin/recetas";
	}

	@PostMapping("/{id}/detalles")
	public String agregarDetalle(@PathVariable Long id,
			@Valid @ModelAttribute("nuevoDetalle") DetalleRecetaDTO detalle, BindingResult resultado,
			Model model, RedirectAttributes ra) {
		detalle.setRecetaId(id);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en detalle recetaId={}: {} errores", id,
					resultado.getErrorCount());
			model.addAttribute("receta", recetaService.obtenerPorId(id));
			cargarDetalle(model, id, detalle);
			model.addAttribute("edicion", true);
			return "admin/recetas/receta-form";
		}
		recetaService.agregarDetalle(detalle);
		log.info("Detalle agregado recetaId={}", id);
		ra.addFlashAttribute("exito", "Ingrediente agregado a la receta.");
		return "redirect:/admin/recetas/" + id + "/editar";
	}

	@PostMapping("/{id}/detalles/{detalleId}/eliminar")
	public String eliminarDetalle(@PathVariable Long id, @PathVariable Long detalleId,
			RedirectAttributes ra) {
		recetaService.eliminarDetalle(detalleId);
		log.info("Detalle eliminado: id={} recetaId={}", detalleId, id);
		ra.addFlashAttribute("exito", "Ingrediente quitado de la receta.");
		return "redirect:/admin/recetas/" + id + "/editar";
	}

	private void cargarDetalle(Model model, Long recetaId, DetalleRecetaDTO detalle) {
		model.addAttribute("detalles", recetaService.listarDetalles(recetaId));
		model.addAttribute("ingredientes", ingredienteService.listar().stream()
				.filter(i -> i.isActivo()).toList());
		model.addAttribute("unidades", UnidadMedida.values());
		model.addAttribute("nuevoDetalle", detalle);
	}

	private DetalleRecetaDTO newDetalle(Long recetaId) {
		DetalleRecetaDTO detalle = new DetalleRecetaDTO();
		detalle.setRecetaId(recetaId);
		return detalle;
	}

}
