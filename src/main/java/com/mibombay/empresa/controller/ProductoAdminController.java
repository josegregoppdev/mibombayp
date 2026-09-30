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

import com.mibombay.empresa.dto.ProductoDTO;
import com.mibombay.empresa.model.Categoria;
import com.mibombay.empresa.service.ProductoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/productos")
@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
public class ProductoAdminController {

	private static final Logger log = LoggerFactory.getLogger(ProductoAdminController.class);

	private final ProductoService productoService;

	public ProductoAdminController(ProductoService productoService) {
		this.productoService = productoService;
	}

	@GetMapping
	public String listar(Model model) {
		log.debug("GET /admin/productos");
		model.addAttribute("productos", productoService.listar());
		return "admin/productos/productos";
	}

	@GetMapping("/nuevo")
	public String nuevo(Model model) {
		log.debug("GET /admin/productos/nuevo");
		model.addAttribute("producto", new ProductoDTO());
		model.addAttribute("categorias", Categoria.values());
		model.addAttribute("edicion", false);
		return "admin/productos/producto-form";
	}

	@PostMapping
	public String crear(@Valid @ModelAttribute("producto") ProductoDTO producto,
			BindingResult resultado, Model model, RedirectAttributes ra) {
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en alta: {} errores", resultado.getErrorCount());
			model.addAttribute("categorias", Categoria.values());
			model.addAttribute("edicion", false);
			return "admin/productos/producto-form";
		}
		ProductoDTO respuesta = productoService.crear(producto);
		log.info("Alta producto: id={} nombre={} categoria={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.getCategoria());
		ra.addFlashAttribute("exito", "Producto creado correctamente.");
		return "redirect:/admin/productos";
	}

	@GetMapping("/{id}/editar")
	public String editar(@PathVariable Long id, Model model) {
		log.debug("GET /admin/productos/{}/editar", id);
		model.addAttribute("producto", productoService.obtenerPorId(id));
		model.addAttribute("categorias", Categoria.values());
		model.addAttribute("edicion", true);
		return "admin/productos/producto-form";
	}

	@PostMapping("/{id}")
	public String actualizar(@PathVariable Long id,
			@Valid @ModelAttribute("producto") ProductoDTO producto, BindingResult resultado,
			Model model, RedirectAttributes ra) {
		producto.setId(id);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en edición id={}: {} errores", id, resultado.getErrorCount());
			model.addAttribute("categorias", Categoria.values());
			model.addAttribute("edicion", true);
			return "admin/productos/producto-form";
		}
		ProductoDTO respuesta = productoService.actualizar(id, producto);
		log.info("Edición producto: id={} nombre={} categoria={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.getCategoria());
		ra.addFlashAttribute("exito", "Producto actualizado correctamente.");
		return "redirect:/admin/productos";
	}

	@PostMapping("/{id}/activo")
	public String alternarActivo(@PathVariable Long id, RedirectAttributes ra) {
		ProductoDTO respuesta = productoService.alternarActivo(id);
		log.info("Cambio estado: id={} nombre={} activo={}", respuesta.getId(), respuesta.getNombre(),
				respuesta.isActivo());
		ra.addFlashAttribute("exito", "Estado del producto actualizado correctamente.");
		return "redirect:/admin/productos";
	}

}
