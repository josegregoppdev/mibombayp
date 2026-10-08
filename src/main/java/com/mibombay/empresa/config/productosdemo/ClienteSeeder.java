package com.mibombay.empresa.config.productosdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mibombay.empresa.config.DataSeeder;
import com.mibombay.empresa.model.Cliente;
import com.mibombay.empresa.repository.ClienteRepository;

@Component
@Order(5)
public class ClienteSeeder implements DataSeeder {

	private static final Logger log = LoggerFactory.getLogger(ClienteSeeder.class);

	private final ClienteRepository clienteRepository;

	public ClienteSeeder(ClienteRepository clienteRepository) {
		this.clienteRepository = clienteRepository;
	}

	@Override
	public void sembrar() {
		crearClienteSiNoExiste("María Fernanda", "López", "1023456789", "Cra 10 #20-30", "3001234567",
				"maria.lopez@email.com");
		crearClienteSiNoExiste("Carlos Andrés", "Gómez", "79456123", "Calle 5 #12-45", "3109876543",
				"carlos.gomez@email.com");
		crearClienteSiNoExiste("Restaurante El Sabor S.A.S.", null, "900123456-1", "Av. Principal #45-67",
				"6012345678", "contacto@elsabor.com");
		crearClienteSiNoExiste("Laura Patricia", "Martínez", "1058765432", "Cl 80 #34-12", "3201112233",
				"laura.martinez@email.com");
		crearClienteSiNoExiste("José Ricardo", "Hernández", "80123456", "Tv 3 #45-12", "3154445566",
				"jose.hernandez@email.com");
		crearClienteSiNoExiste("Distribuciones El Éxito Ltda", null, "830123456-2",
				"Zona Industrial Calle 18 #68-40", "6017654321", "pedidos@exito.com");
		crearClienteSiNoExiste("Ana Sofía", "Ramírez", "1070123456", "Cra 15 #7-89", "3227778899",
				"ana.ramirez@email.com");
		crearClienteSiNoExiste("Luis Felipe", "Torres", "71234567", "Cl 45 #20-15", "3183334444",
				"luis.torres@email.com");
		crearClienteSiNoExiste("Blanca", "Quintero", "39456789", "Cra 22 #9-10", "3005556677",
				"blanca.quintero@email.com");
		crearClienteSiNoExiste("Panadería Dulce Hogar S.A.", null, "901987654-3", "Av. Bolívar #23-50",
				"6019876543", "info@dulcehogar.com");
	}

	private void crearClienteSiNoExiste(String nombre, String apellido, String dniNit, String direccion,
			String telefono, String email) {
		if (clienteRepository.existsByDniNitIgnoreCase(dniNit)) {
			return;
		}
		Cliente cliente = new Cliente();
		cliente.setNombre(nombre);
		cliente.setApellido(apellido);
		cliente.setDniNit(dniNit);
		cliente.setDireccion(direccion);
		cliente.setTelefono(telefono);
		cliente.setEmail(email);
		cliente.setActivo(true);
		clienteRepository.save(cliente);
		log.info("Cliente semilla creado: {} {}", nombre, apellido != null ? apellido : "(jurídico)");
	}

}
