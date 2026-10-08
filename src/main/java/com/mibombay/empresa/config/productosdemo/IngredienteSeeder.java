package com.mibombay.empresa.config.productosdemo;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mibombay.empresa.config.DataSeeder;
import com.mibombay.empresa.model.Ingrediente;
import com.mibombay.empresa.model.UnidadMedida;
import com.mibombay.empresa.repository.IngredienteRepository;

@Component
@Order(1)
public class IngredienteSeeder implements DataSeeder {

	private static final Logger log = LoggerFactory.getLogger(IngredienteSeeder.class);

	private final IngredienteRepository ingredienteRepository;

	public IngredienteSeeder(IngredienteRepository ingredienteRepository) {
		this.ingredienteRepository = ingredienteRepository;
	}

	@Override
	public void sembrar() {
		crearIngredienteSiNoExiste("Pan perro", UnidadMedida.UD, "100", "20", "800", "1500", "0", "0", false);
		crearIngredienteSiNoExiste("Salchicha", UnidadMedida.UD, "120", "30", "1000", "2000", "0", "0", false);
		crearIngredienteSiNoExiste("Pan hamburguesa", UnidadMedida.UD, "80", "20", "1200", "2200", "0", "0", false);
		crearIngredienteSiNoExiste("Carne molida", UnidadMedida.KG, "15", "5", "18000", "32000", "0", "0", false);
		crearIngredienteSiNoExiste("Queso rallado", UnidadMedida.KG, "5", "2", "16000", "28000", "0", "0", false);
		crearIngredienteSiNoExiste("Papa frita congelada", UnidadMedida.KG, "20", "5", "8000", "14000", "0", "0", false);
		crearIngredienteSiNoExiste("Lechuga", UnidadMedida.KG, "4", "1", "3500", "6000", "0", "0", false);
		crearIngredienteSiNoExiste("Mayonesa", UnidadMedida.L, "10", "3", "8000", "14000", "0.02", "800", true);
		crearIngredienteSiNoExiste("Ketchup", UnidadMedida.L, "10", "3", "6500", "11000", "0.02", "800", true);
		crearIngredienteSiNoExiste("Mostaza", UnidadMedida.L, "5", "2", "7000", "12000", "0.01", "800", true);
		crearIngredienteSiNoExiste("Cebolla", UnidadMedida.KG, "30", "8", "3000", "5500", "0", "0", false);
		crearIngredienteSiNoExiste("Tomate", UnidadMedida.KG, "15", "4", "4000", "7500", "0", "0", false);
		crearIngredienteSiNoExiste("Papa fresca", UnidadMedida.KG, "40", "10", "2500", "4500", "0", "0", false);
		crearIngredienteSiNoExiste("Aceite de cocina", UnidadMedida.L, "20", "5", "12000", "20000", "0", "0", false);
		crearIngredienteSiNoExiste("Huevo", UnidadMedida.UD, "120", "30", "450", "800", "0", "0", false);
		crearIngredienteSiNoExiste("Repollo", UnidadMedida.KG, "10", "3", "2500", "4500", "0", "0", false);
		crearIngredienteSiNoExiste("Tocineta", UnidadMedida.KG, "6", "2", "22000", "36000", "0.03", "2500", true);
		crearIngredienteSiNoExiste("Queso en lonjas", UnidadMedida.KG, "5", "2", "15000", "26000", "0", "0", false);
		crearIngredienteSiNoExiste("Arepa", UnidadMedida.UD, "60", "15", "600", "1200", "0", "0", false);
		crearIngredienteSiNoExiste("Maíz dulce", UnidadMedida.KG, "8", "3", "6000", "10000", "0.04", "1500", true);
		crearIngredienteSiNoExiste("Salsa de ajo", UnidadMedida.L, "6", "2", "7000", "12000", "0.01", "1000", true);
		crearIngredienteSiNoExiste("Salsa picante", UnidadMedida.L, "6", "2", "7500", "13000", "0.01", "1000", true);
		crearIngredienteSiNoExiste("Pechuga de pollo", UnidadMedida.KG, "10", "3", "12000", "22000", "0", "0", false);
		crearIngredienteSiNoExiste("Jalapeño", UnidadMedida.KG, "4", "1", "14000", "24000", "0.02", "1500", true);
		crearIngredienteSiNoExiste("Sal", UnidadMedida.KG, "10", "2", "2000", "4000", "0", "0", false);
	}

	private void crearIngredienteSiNoExiste(String nombre, UnidadMedida unidadMedida, String stockActual,
			String stockMinimo, String valorCompra, String valorVenta, String porcionAdicional, String precioAdicional,
			boolean esAdicional) {
		if (ingredienteRepository.existsByNombreIgnoreCase(nombre)) {
			return;
		}
		Ingrediente ingrediente = new Ingrediente();
		ingrediente.setNombre(nombre);
		ingrediente.setUnidadMedida(unidadMedida);
		ingrediente.setStockActual(new BigDecimal(stockActual));
		ingrediente.setStockMinimo(new BigDecimal(stockMinimo));
		ingrediente.setValorCompra(new BigDecimal(valorCompra));
		ingrediente.setValorVenta(new BigDecimal(valorVenta));
		ingrediente.setPorcionAdicional(new BigDecimal(porcionAdicional));
		ingrediente.setPrecioAdicional(new BigDecimal(precioAdicional));
		ingrediente.setEsAdicional(esAdicional);
		ingrediente.setActivo(true);
		ingredienteRepository.save(ingrediente);
		log.info("Ingrediente semilla creado: {} {}", nombre, unidadMedida);
	}

}
