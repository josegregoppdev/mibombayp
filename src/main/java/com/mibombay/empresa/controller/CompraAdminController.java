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

import com.mibombay.empresa.dto.CompraDTO;
import com.mibombay.empresa.dto.CompraDetalleDTO;
import com.mibombay.empresa.model.TipoItem;
import com.mibombay.empresa.service.CompraService;
import com.mibombay.empresa.service.IngredienteService;
import com.mibombay.empresa.service.ProductoService;
import com.mibombay.empresa.service.ProveedorService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/compras")
@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
public class CompraAdminController {

	private static final Logger log = LoggerFactory.getLogger(CompraAdminController.class);

	private final CompraService compraService;
	private final ProveedorService proveedorService;
	private final IngredienteService ingredienteService;
	private final ProductoService productoService;

	public CompraAdminController(CompraService compraService, ProveedorService proveedorService,
			IngredienteService ingredienteService, ProductoService productoService) {
		this.compraService = compraService;
		this.proveedorService = proveedorService;
		this.ingredienteService = ingredienteService;
		this.productoService = productoService;
	}

	@GetMapping
	public String listar(Model model) {
		log.debug("GET /admin/compras");
		model.addAttribute("compras", compraService.listar());
		return "admin/compras/compras";
	}

	@GetMapping("/nuevo")
	public String nuevo(Model model) {
		log.debug("GET /admin/compras/nuevo");
		model.addAttribute("compra", new CompraDTO());
		model.addAttribute("edicion", false);
		cargarProveedores(model);
		return "admin/compras/compra-form";
	}

	@PostMapping
	public String crear(@Valid @ModelAttribute("compra") CompraDTO compra, BindingResult resultado, Model model,
			RedirectAttributes ra) {
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en alta de compra: {} errores", resultado.getErrorCount());
			model.addAttribute("edicion", false);
			cargarProveedores(model);
			return "admin/compras/compra-form";
		}
		CompraDTO respuesta = compraService.crear(compra);
		log.info("Alta compra: id={} proveedorId={}", respuesta.getId(), respuesta.getProveedorId());
		ra.addFlashAttribute("exito", "Compra creada. Agrega las líneas y confirma para ajustar el inventario.");
		return "redirect:/admin/compras/" + respuesta.getId() + "/editar";
	}

	@GetMapping("/{id}/editar")
	public String editar(@PathVariable Long id, Model model) {
		log.debug("GET /admin/compras/{}/editar", id);
		cargarContexto(model, id);
		siNoHay(model, "nuevoDetalleIngrediente", nuevoDetalle(id, TipoItem.INGREDIENTE));
		siNoHay(model, "nuevoDetalleProducto", nuevoDetalle(id, TipoItem.PRODUCTO));
		return "admin/compras/compra-form";
	}

	@PostMapping("/{id}")
	public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("compra") CompraDTO compra,
			BindingResult resultado, Model model, RedirectAttributes ra) {
		compra.setId(id);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en edición de compra id={}: {} errores", id,
					resultado.getErrorCount());
			cargarContexto(model, id);
			siNoHay(model, "nuevoDetalleIngrediente", nuevoDetalle(id, TipoItem.INGREDIENTE));
			siNoHay(model, "nuevoDetalleProducto", nuevoDetalle(id, TipoItem.PRODUCTO));
			return "admin/compras/compra-form";
		}
		CompraDTO respuesta = compraService.actualizar(id, compra);
		log.info("Edición compra: id={} proveedorId={}", respuesta.getId(), respuesta.getProveedorId());
		ra.addFlashAttribute("exito", "Compra actualizada correctamente.");
		return "redirect:/admin/compras/" + id + "/editar";
	}

	@PostMapping("/{id}/detalles")
	public String agregarDetalleIngrediente(@PathVariable Long id,
			@Valid @ModelAttribute("nuevoDetalleIngrediente") CompraDetalleDTO detalle, BindingResult resultado,
			Model model, RedirectAttributes ra) {
		detalle.setCompraId(id);
		detalle.setTipo(TipoItem.INGREDIENTE);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en detalle de ingrediente compraId={}: {} errores", id,
					resultado.getErrorCount());
			cargarContexto(model, id);
			siNoHay(model, "nuevoDetalleProducto", nuevoDetalle(id, TipoItem.PRODUCTO));
			return "admin/compras/compra-form";
		}
		compraService.agregarDetalle(detalle);
		log.info("Detalle de ingrediente agregado: compraId={}", id);
		ra.addFlashAttribute("exito", "Ingrediente agregado a la compra.");
		return "redirect:/admin/compras/" + id + "/editar";
	}

	@PostMapping("/{id}/detalles-producto")
	public String agregarDetalleProducto(@PathVariable Long id,
			@Valid @ModelAttribute("nuevoDetalleProducto") CompraDetalleDTO detalle, BindingResult resultado,
			Model model, RedirectAttributes ra) {
		detalle.setCompraId(id);
		detalle.setTipo(TipoItem.PRODUCTO);
		if (resultado.hasErrors()) {
			log.debug("Validación @Valid fallida en detalle de producto compraId={}: {} errores", id,
					resultado.getErrorCount());
			cargarContexto(model, id);
			siNoHay(model, "nuevoDetalleIngrediente", nuevoDetalle(id, TipoItem.INGREDIENTE));
			return "admin/compras/compra-form";
		}
		compraService.agregarDetalle(detalle);
		log.info("Detalle de producto agregado: compraId={}", id);
		ra.addFlashAttribute("exito", "Producto agregado a la compra.");
		return "redirect:/admin/compras/" + id + "/editar";
	}

	@PostMapping("/{id}/detalles/{detalleId}/eliminar")
	public String eliminarDetalle(@PathVariable Long id, @PathVariable Long detalleId, RedirectAttributes ra) {
		compraService.eliminarDetalle(detalleId);
		log.info("Detalle eliminado: id={} compraId={}", detalleId, id);
		ra.addFlashAttribute("exito", "Línea quitada de la compra.");
		return "redirect:/admin/compras/" + id + "/editar";
	}

	@PostMapping("/{id}/confirmar")
	public String confirmar(@PathVariable Long id, RedirectAttributes ra) {
		log.debug("POST /admin/compras/{}/confirmar", id);
		CompraDTO respuesta = compraService.confirmar(id);
		log.info("Confirmación de compra: id={} total={}", respuesta.getId(), respuesta.getTotal());
		ra.addFlashAttribute("exito", "Compra confirmada: inventario, costos y recetas actualizados.");
		return "redirect:/admin/compras/" + id + "/editar";
	}

	private void cargarContexto(Model model, Long compraId) {
		if (!model.containsAttribute("compra")) {
			model.addAttribute("compra", compraService.obtenerPorId(compraId));
		}
		model.addAttribute("edicion", true);
		cargarProveedores(model);
		model.addAttribute("detalles", compraService.listarDetalles(compraId));
		model.addAttribute("ingredientes", ingredienteService.listar().stream()
				.filter(i -> i.isActivo()).toList());
		model.addAttribute("productos", productoService.listar().stream()
				.filter(p -> p.isActivo()).toList());
	}

	private void cargarProveedores(Model model) {
		model.addAttribute("proveedores", proveedorService.listar().stream()
				.filter(p -> p.isActivo()).toList());
	}

	private CompraDetalleDTO nuevoDetalle(Long compraId, TipoItem tipo) {
		CompraDetalleDTO detalle = new CompraDetalleDTO();
		detalle.setCompraId(compraId);
		detalle.setTipo(tipo);
		return detalle;
	}

	private void siNoHay(Model model, String nombre, Object valor) {
		if (!model.containsAttribute(nombre)) {
			model.addAttribute(nombre, valor);
		}
	}

}
