package com.mibombay.empresa.controller;

import java.util.List;

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

import com.mibombay.empresa.dto.ProductoConRecetaDTO;
import com.mibombay.empresa.dto.RecetaDTO;
import com.mibombay.empresa.service.ProductoConRecetaService;
import com.mibombay.empresa.service.RecetaService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/productos-con-receta")
@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
public class ProductoConRecetaAdminController {

	private static final Logger log = LoggerFactory.getLogger(ProductoConRecetaAdminController.class);

	private final ProductoConRecetaService productoService;
	private final RecetaService recetaService;

	public ProductoConRecetaAdminController(ProductoConRecetaService productoService,
			RecetaService recetaService) {
		this.productoService = productoService;
		this.recetaService = recetaService;
	}

	@GetMapping
	public String listar(Model model) {
		log.debug("GET /admin/productos-con-receta");
		model.addAttribute("productos", productoService.listar());
		return "admin/productos-con-receta/productos-con-receta";
	}

	@GetMapping("/nuevo")
	public String nuevo(Model model) {
		log.debug("GET /admin/productos-con-receta/nuevo");
		model.addAttribute("producto", new ProductoConRecetaDTO());
		model.addAttribute("recetas", recetasDisponibles(new ProductoConRecetaDTO()));
		model.addAttribute("edicion", false);
		return "admin/productos-con-receta/producto-con-receta-form";
	}

	@PostMapping
	public String crear(@Valid @ModelAttribute("producto") ProductoConRecetaDTO producto,
			BindingResult resultado, Model model, RedirectAttributes ra) {
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en alta: {} errores", resultado.getErrorCount());
			model.addAttribute("recetas", recetasDisponibles(producto));
			model.addAttribute("edicion", false);
			return "admin/productos-con-receta/producto-con-receta-form";
		}
		ProductoConRecetaDTO respuesta = productoService.crear(producto);
		log.info("Alta producto con receta: id={} nombre={}", respuesta.getId(), respuesta.getNombre());
		ra.addFlashAttribute("exito", "Producto creado correctamente.");
		return "redirect:/admin/productos-con-receta";
	}

	@GetMapping("/{id}/editar")
	public String editar(@PathVariable Long id, Model model) {
		log.debug("GET /admin/productos-con-receta/{}/editar", id);
		ProductoConRecetaDTO producto = productoService.obtenerPorId(id);
		model.addAttribute("producto", producto);
		model.addAttribute("recetas", recetasDisponibles(producto));
		model.addAttribute("edicion", true);
		return "admin/productos-con-receta/producto-con-receta-form";
	}

	@PostMapping("/{id}")
	public String actualizar(@PathVariable Long id,
			@Valid @ModelAttribute("producto") ProductoConRecetaDTO producto, BindingResult resultado,
			Model model, RedirectAttributes ra) {
		producto.setId(id);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en edición id={}: {} errores", id, resultado.getErrorCount());
			model.addAttribute("recetas", recetasDisponibles(producto));
			model.addAttribute("edicion", true);
			return "admin/productos-con-receta/producto-con-receta-form";
		}
		ProductoConRecetaDTO respuesta = productoService.actualizar(id, producto);
		log.info("Edición producto con receta: id={} nombre={}", respuesta.getId(), respuesta.getNombre());
		ra.addFlashAttribute("exito", "Producto actualizado correctamente.");
		return "redirect:/admin/productos-con-receta";
	}

	@PostMapping("/{id}/activo")
	public String alternarActivo(@PathVariable Long id, RedirectAttributes ra) {
		ProductoConRecetaDTO respuesta = productoService.alternarActivo(id);
		log.info("Cambio estado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		ra.addFlashAttribute("exito", "Estado del producto actualizado correctamente.");
		return "redirect:/admin/productos-con-receta";
	}

	private List<RecetaDTO> recetasDisponibles(ProductoConRecetaDTO producto) {
		return recetaService.listar().stream()
				.filter(r -> r.isActivo())
				.filter(r -> !r.isEnUso()
						|| (producto.getRecetaId() != null && r.getId().equals(producto.getRecetaId())))
				.toList();
	}

}
