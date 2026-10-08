package com.mibombay.empresa.config.productosdemo;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mibombay.empresa.config.DataSeeder;
import com.mibombay.empresa.model.ProductoConReceta;
import com.mibombay.empresa.model.Receta;
import com.mibombay.empresa.repository.ProductoConRecetaRepository;
import com.mibombay.empresa.repository.RecetaRepository;

@Component
@Order(3)
public class ProductoConRecetaSeeder implements DataSeeder {

	private static final Logger log = LoggerFactory.getLogger(ProductoConRecetaSeeder.class);

	private final ProductoConRecetaRepository productoConRecetaRepository;
	private final RecetaRepository recetaRepository;

	public ProductoConRecetaSeeder(ProductoConRecetaRepository productoConRecetaRepository,
			RecetaRepository recetaRepository) {
		this.productoConRecetaRepository = productoConRecetaRepository;
		this.recetaRepository = recetaRepository;
	}

	@Override
	public void sembrar() {
		crearProductoConRecetaSiNoExiste("Perro caliente clásico", "4500");
		crearProductoConRecetaSiNoExiste("Perro especial con tocineta", "6000");
		crearProductoConRecetaSiNoExiste("Perro colombiano", "4800");
		crearProductoConRecetaSiNoExiste("Hamburguesa clásica", "9500");
		crearProductoConRecetaSiNoExiste("Hamburguesa doble con tocineta", "13500");
		crearProductoConRecetaSiNoExiste("Hamburguesa de pollo", "9000");
		crearProductoConRecetaSiNoExiste("Hamburguesa con huevo", "9500");
		crearProductoConRecetaSiNoExiste("Salchipapa", "9000");
		crearProductoConRecetaSiNoExiste("Salchipapa especial con huevo", "11000");
		crearProductoConRecetaSiNoExiste("Papas con queso y tocineta", "8500");
		crearProductoConRecetaSiNoExiste("Papas fritas clásicas", "3500");
		crearProductoConRecetaSiNoExiste("Arepa con queso", "3000");
	}

	private void crearProductoConRecetaSiNoExiste(String nombre, String precioVentaTexto) {
		if (productoConRecetaRepository.existsByNombreIgnoreCase(nombre)) {
			return;
		}
		Optional<Receta> recetaBuscada = recetaRepository.findByNombreIgnoreCase(nombre);
		if (recetaBuscada.isEmpty()) {
			log.warn("Producto con receta semilla omitido {}, no existe la receta: {}", nombre, nombre);
			return;
		}
		Receta receta = recetaBuscada.get();
		if (receta.isEnUso() || productoConRecetaRepository.existsByRecetaId(receta.getId())) {
			log.warn("Producto con receta semilla omitido {}, la receta ya está en uso: {}", nombre, nombre);
			return;
		}
		BigDecimal precioVenta = new BigDecimal(precioVentaTexto);
		if (precioVenta.compareTo(receta.getCosto()) < 0) {
			log.warn("Producto con receta semilla omitido {}, precio {} menor al costo {}", nombre, precioVenta,
					receta.getCosto());
			return;
		}
		ProductoConReceta producto = new ProductoConReceta();
		producto.setNombre(nombre);
		producto.setReceta(receta);
		producto.setPrecioVenta(precioVenta);
		producto.setActivo(true);
		receta.setEnUso(true);
		recetaRepository.save(receta);
		productoConRecetaRepository.save(producto);
		log.info("Producto con receta semilla creado: {} receta={} precio={}", nombre, receta.getNombre(),
				precioVenta);
	}

}
