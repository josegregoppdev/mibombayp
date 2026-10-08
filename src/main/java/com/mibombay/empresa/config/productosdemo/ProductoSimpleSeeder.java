package com.mibombay.empresa.config.productosdemo;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mibombay.empresa.config.DataSeeder;
import com.mibombay.empresa.model.Categoria;
import com.mibombay.empresa.model.Producto;
import com.mibombay.empresa.repository.ProductoRepository;

@Component
@Order(4)
public class ProductoSimpleSeeder implements DataSeeder {

	private static final Logger log = LoggerFactory.getLogger(ProductoSimpleSeeder.class);

	private final ProductoRepository productoRepository;

	public ProductoSimpleSeeder(ProductoRepository productoRepository) {
		this.productoRepository = productoRepository;
	}

	@Override
	public void sembrar() {
		crearProductoSiNoExiste("Coca-Cola 400 ml", Categoria.BEBIDAS, "48", "12", "1800", "3000");
		crearProductoSiNoExiste("Pepsi 400 ml", Categoria.BEBIDAS, "48", "12", "1700", "3000");
		crearProductoSiNoExiste("Manzana Postobón 400 ml", Categoria.BEBIDAS, "36", "12", "1600", "2800");
		crearProductoSiNoExiste("Colombiana 400 ml", Categoria.BEBIDAS, "36", "12", "1600", "2800");
		crearProductoSiNoExiste("Agua mineral 600 ml", Categoria.BEBIDAS, "36", "12", "1200", "2500");
		crearProductoSiNoExiste("Jugo Hit 400 ml", Categoria.BEBIDAS, "24", "6", "1700", "3000");
		crearProductoSiNoExiste("Big Cola 1.5 L", Categoria.BEBIDAS, "24", "6", "3500", "6000");
		crearProductoSiNoExiste("Cerveza Poker lata 330 ml", Categoria.CERVEZAS, "36", "12", "2500", "4500");
		crearProductoSiNoExiste("Galletas Oreo paquete", Categoria.POSTRES, "24", "6", "2000", "3500");
		crearProductoSiNoExiste("Papas fritas paquete 50 g", Categoria.EXTRAS, "30", "10", "2200", "4000");
	}

	private void crearProductoSiNoExiste(String nombre, Categoria categoria, String stockActual, String stockMinimo,
			String costo, String precioVenta) {
		if (productoRepository.existsByNombreIgnoreCase(nombre)) {
			return;
		}
		BigDecimal precio = new BigDecimal(precioVenta);
		if (precio.compareTo(new BigDecimal(costo)) < 0) {
			log.warn("Producto semilla omitido {}, precio {} menor al costo {}", nombre, precioVenta, costo);
			return;
		}
		Producto producto = new Producto();
		producto.setNombre(nombre);
		producto.setCategoria(categoria);
		producto.setStockActual(new BigDecimal(stockActual));
		producto.setStockMinimo(new BigDecimal(stockMinimo));
		producto.setCosto(new BigDecimal(costo));
		producto.setPrecioVenta(precio);
		producto.setActivo(true);
		productoRepository.save(producto);
		log.info("Producto semilla creado: {} {} precio={}", nombre, categoria, precioVenta);
	}

}
