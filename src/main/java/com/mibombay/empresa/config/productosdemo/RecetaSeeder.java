package com.mibombay.empresa.config.productosdemo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mibombay.empresa.config.DataSeeder;
import com.mibombay.empresa.model.DetalleReceta;
import com.mibombay.empresa.model.Ingrediente;
import com.mibombay.empresa.model.Receta;
import com.mibombay.empresa.repository.DetalleRecetaRepository;
import com.mibombay.empresa.repository.IngredienteRepository;
import com.mibombay.empresa.repository.RecetaRepository;

@Component
@Order(2)
public class RecetaSeeder implements DataSeeder {

	private static final Logger log = LoggerFactory.getLogger(RecetaSeeder.class);

	private final RecetaRepository recetaRepository;
	private final DetalleRecetaRepository detalleRecetaRepository;
	private final IngredienteRepository ingredienteRepository;

	public RecetaSeeder(RecetaRepository recetaRepository, DetalleRecetaRepository detalleRecetaRepository,
			IngredienteRepository ingredienteRepository) {
		this.recetaRepository = recetaRepository;
		this.detalleRecetaRepository = detalleRecetaRepository;
		this.ingredienteRepository = ingredienteRepository;
	}

	@Override
	public void sembrar() {
		crearRecetaSiNoExiste("Perro caliente clásico",
				new Linea("Pan perro", "1"),
				new Linea("Salchicha", "1"),
				new Linea("Cebolla", "0.02"),
				new Linea("Ketchup", "0.02"),
				new Linea("Mostaza", "0.01"));
		crearRecetaSiNoExiste("Perro especial con tocineta",
				new Linea("Pan perro", "1"),
				new Linea("Salchicha", "1"),
				new Linea("Tocineta", "0.02"),
				new Linea("Queso rallado", "0.02"),
				new Linea("Salsa de ajo", "0.01"),
				new Linea("Ketchup", "0.015"));
		crearRecetaSiNoExiste("Perro colombiano",
				new Linea("Pan perro", "1"),
				new Linea("Salchicha", "1"),
				new Linea("Repollo", "0.05"),
				new Linea("Cebolla", "0.02"),
				new Linea("Mayonesa", "0.015"),
				new Linea("Salsa picante", "0.01"));
		crearRecetaSiNoExiste("Hamburguesa clásica",
				new Linea("Pan hamburguesa", "1"),
				new Linea("Carne molida", "0.15"),
				new Linea("Queso en lonjas", "0.03"),
				new Linea("Lechuga", "0.03"),
				new Linea("Tomate", "0.04"),
				new Linea("Cebolla", "0.02"),
				new Linea("Ketchup", "0.015"),
				new Linea("Mostaza", "0.01"));
		crearRecetaSiNoExiste("Hamburguesa doble con tocineta",
				new Linea("Pan hamburguesa", "1"),
				new Linea("Carne molida", "0.25"),
				new Linea("Tocineta", "0.04"),
				new Linea("Queso rallado", "0.03"),
				new Linea("Cebolla", "0.02"),
				new Linea("Salsa de ajo", "0.01"));
		crearRecetaSiNoExiste("Hamburguesa de pollo",
				new Linea("Pan hamburguesa", "1"),
				new Linea("Pechuga de pollo", "0.18"),
				new Linea("Lechuga", "0.03"),
				new Linea("Tomate", "0.04"),
				new Linea("Mayonesa", "0.02"),
				new Linea("Queso en lonjas", "0.02"));
		crearRecetaSiNoExiste("Hamburguesa con huevo",
				new Linea("Pan hamburguesa", "1"),
				new Linea("Carne molida", "0.12"),
				new Linea("Huevo", "1"),
				new Linea("Queso rallado", "0.03"),
				new Linea("Lechuga", "0.03"),
				new Linea("Tomate", "0.04"),
				new Linea("Cebolla", "0.02"),
				new Linea("Salsa picante", "0.01"));
		crearRecetaSiNoExiste("Salchipapa",
				new Linea("Papa frita congelada", "0.3"),
				new Linea("Salchicha", "2"),
				new Linea("Mayonesa", "0.02"),
				new Linea("Ketchup", "0.02"),
				new Linea("Salsa picante", "0.01"));
		crearRecetaSiNoExiste("Salchipapa especial con huevo",
				new Linea("Papa frita congelada", "0.35"),
				new Linea("Salchicha", "2"),
				new Linea("Huevo", "1"),
				new Linea("Queso rallado", "0.04"),
				new Linea("Salsa de ajo", "0.015"));
		crearRecetaSiNoExiste("Papas con queso y tocineta",
				new Linea("Papa frita congelada", "0.3"),
				new Linea("Queso rallado", "0.06"),
				new Linea("Tocineta", "0.03"),
				new Linea("Maíz dulce", "0.04"),
				new Linea("Salsa de ajo", "0.01"));
		crearRecetaSiNoExiste("Papas fritas clásicas",
				new Linea("Papa fresca", "0.3"),
				new Linea("Aceite de cocina", "0.05"),
				new Linea("Sal", "0.005"));
		crearRecetaSiNoExiste("Arepa con queso",
				new Linea("Arepa", "1"),
				new Linea("Queso rallado", "0.05"));
		crearRecetaSiNoExiste("Arepa con pollo",
				new Linea("Arepa", "1"),
				new Linea("Pechuga de pollo", "0.1"),
				new Linea("Queso rallado", "0.03"),
				new Linea("Cebolla", "0.02"),
				new Linea("Salsa de ajo", "0.01"));
		crearRecetaSiNoExiste("Ensalada fresca",
				new Linea("Lechuga", "0.15"),
				new Linea("Tomate", "0.1"),
				new Linea("Cebolla", "0.05"),
				new Linea("Maíz dulce", "0.05"),
				new Linea("Mayonesa", "0.02"),
				new Linea("Sal", "0.002"));
		crearRecetaSiNoExiste("Pechuga a la plancha con ensalada",
				new Linea("Pechuga de pollo", "0.25"),
				new Linea("Aceite de cocina", "0.02"),
				new Linea("Sal", "0.003"),
				new Linea("Lechuga", "0.08"),
				new Linea("Tomate", "0.06"),
				new Linea("Cebolla", "0.03"));
	}

	private void crearRecetaSiNoExiste(String nombre, Linea... lineas) {
		if (recetaRepository.existsByNombreIgnoreCase(nombre)) {
			return;
		}
		List<Ingrediente> ingredientes = new ArrayList<>();
		List<BigDecimal> cantidades = new ArrayList<>();
		for (Linea linea : lineas) {
			Optional<Ingrediente> ingrediente = ingredienteRepository.findByNombreIgnoreCase(linea.ingrediente());
			if (ingrediente.isEmpty()) {
				log.warn("Receta semilla omitida {}, falta el ingrediente: {}", nombre, linea.ingrediente());
				return;
			}
			ingredientes.add(ingrediente.get());
			cantidades.add(new BigDecimal(linea.cantidad()));
		}
		BigDecimal costo = BigDecimal.ZERO;
		for (int i = 0; i < ingredientes.size(); i++) {
			costo = costo.add(cantidades.get(i).multiply(ingredientes.get(i).getValorCompra()));
		}
		Receta receta = new Receta();
		receta.setNombre(nombre);
		receta.setCosto(costo);
		receta.setActivo(true);
		receta = recetaRepository.save(receta);
		for (int i = 0; i < ingredientes.size(); i++) {
			Ingrediente ingrediente = ingredientes.get(i);
			DetalleReceta detalle = new DetalleReceta();
			detalle.setReceta(receta);
			detalle.setIngrediente(ingrediente);
			detalle.setCantidad(cantidades.get(i));
			detalle.setUnidadMedida(ingrediente.getUnidadMedida());
			detalleRecetaRepository.save(detalle);
		}
		log.info("Receta semilla creada: {} costo={} lineas={}", nombre, costo, lineas.length);
	}

	private record Linea(String ingrediente, String cantidad) {
	}

}
