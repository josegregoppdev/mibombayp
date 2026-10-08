package com.mibombay.empresa.config.productosdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mibombay.empresa.config.DataSeeder;
import com.mibombay.empresa.model.Proveedor;
import com.mibombay.empresa.repository.ProveedorRepository;

@Component
@Order(6)
public class ProveedorSeeder implements DataSeeder {

	private static final Logger log = LoggerFactory.getLogger(ProveedorSeeder.class);

	private final ProveedorRepository proveedorRepository;

	public ProveedorSeeder(ProveedorRepository proveedorRepository) {
		this.proveedorRepository = proveedorRepository;
	}

	@Override
	public void sembrar() {
		crearProveedorSiNoExiste("Proveedor por defecto", null, "000000000-0", null, null, null);
		crearProveedorSiNoExiste("Distribuidora Lácteos del Campo", null, "900456789-1", "Km 3 Vía Mosquera",
				"6014445556", "ventas@lacteos.com");
		crearProveedorSiNoExiste("Carnes Premium S.A.S.", null, "900765432-2", "Av. Industrial #67-89",
				"6013332221", "pedidos@carnes.com");
		crearProveedorSiNoExiste("Verdes Huerta Ltda", null, "811234567-3", "Cl 71 #10-20", "6015556667",
				"contactos@huerta.com");
		crearProveedorSiNoExiste("Pedro Nelson", "Vargas", "162345678", "Cra 50 #13-25", "3112223344",
				"pedro.vargas@email.com");
		crearProveedorSiNoExiste("Bebidas Gaseosas Andinas", null, "890345678-4", "Tv 40 #18-50",
				"6017778889", "distribucion@andinas.com");
		crearProveedorSiNoExiste("Amparo", "Castellanos", "43567890", "Cl 25 #6-40", "3156667788",
				"amparo.castellanos@email.com");
		crearProveedorSiNoExiste("Huevo Fresco del Valle", null, "800654321-5", "Vereda El Rosal",
				"6016665554", "soporte@huevovalle.com");
		crearProveedorSiNoExiste("Rodrigo Iván", "Salazar", "98765432", "Cra 33 #7-15", "3198887766",
				"rodrigo.salazar@email.com");
		crearProveedorSiNoExiste("Aceites Industriales del Norte", null, "812345678-6",
				"Parque Empresarial Norte", "6011119990", "norte@aceites.com");
		crearProveedorSiNoExiste("Gloria Elena", "Mendoza", "51234567", "Cl 100 #14-25", "3134449995",
				"gloria.mendoza@email.com");
	}

	private void crearProveedorSiNoExiste(String nombre, String apellido, String dniNit, String direccion,
			String telefono, String email) {
		if (proveedorRepository.existsByDniNitIgnoreCase(dniNit)) {
			return;
		}
		Proveedor proveedor = new Proveedor();
		proveedor.setNombre(nombre);
		proveedor.setApellido(apellido);
		proveedor.setDniNit(dniNit);
		proveedor.setDireccion(direccion);
		proveedor.setTelefono(telefono);
		proveedor.setEmail(email);
		proveedor.setActivo(true);
		proveedorRepository.save(proveedor);
		log.info("Proveedor semilla creado: {} {}", nombre, apellido != null ? apellido : "(jurídico)");
	}

}
