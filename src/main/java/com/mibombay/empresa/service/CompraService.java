package com.mibombay.empresa.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mibombay.empresa.dto.CompraDTO;
import com.mibombay.empresa.dto.CompraDetalleDTO;
import com.mibombay.empresa.mapper.CompraDetalleMapper;
import com.mibombay.empresa.mapper.CompraMapper;
import com.mibombay.empresa.model.Compra;
import com.mibombay.empresa.model.CompraDetalle;
import com.mibombay.empresa.model.EstadoCompra;
import com.mibombay.empresa.model.Ingrediente;
import com.mibombay.empresa.model.Producto;
import com.mibombay.empresa.model.Proveedor;
import com.mibombay.empresa.model.TipoItem;
import com.mibombay.empresa.repository.CompraDetalleRepository;
import com.mibombay.empresa.repository.CompraRepository;
import com.mibombay.empresa.repository.IngredienteRepository;
import com.mibombay.empresa.repository.ProductoRepository;
import com.mibombay.empresa.repository.ProveedorRepository;
import com.mibombay.empresa.util.ValidacionDatos;

@Service
public class CompraService {

	private static final Logger log = LoggerFactory.getLogger(CompraService.class);

	// esto se deba cambiar a una propiedad configurable en el futuro
	// es decir a la seed
	private static final String DNI_NIT_PROVEEDOR_POR_DEFECTO = "000000000-0";
	private static final String NOMBRE_PROVEEDOR_POR_DEFECTO = "Proveedor por defecto";

	private final CompraRepository compraRepository;
	private final CompraDetalleRepository detalleRepository;
	private final ProveedorRepository proveedorRepository;
	private final IngredienteRepository ingredienteRepository;
	private final ProductoRepository productoRepository;
	private final IngredienteService ingredienteService;
	private final ProductoService productoService;
	private final RecetaService recetaService;
	private final CompraMapper compraMapper;
	private final CompraDetalleMapper detalleMapper;

	public CompraService(CompraRepository compraRepository, CompraDetalleRepository detalleRepository,
			ProveedorRepository proveedorRepository, IngredienteRepository ingredienteRepository,
			ProductoRepository productoRepository, IngredienteService ingredienteService,
			ProductoService productoService, RecetaService recetaService, CompraMapper compraMapper,
			CompraDetalleMapper detalleMapper) {
		this.compraRepository = compraRepository;
		this.detalleRepository = detalleRepository;
		this.proveedorRepository = proveedorRepository;
		this.ingredienteRepository = ingredienteRepository;
		this.productoRepository = productoRepository;
		this.ingredienteService = ingredienteService;
		this.productoService = productoService;
		this.recetaService = recetaService;
		this.compraMapper = compraMapper;
		this.detalleMapper = detalleMapper;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public List<CompraDTO> listar() {
		log.debug("Listando compras");
		List<CompraDTO> compras = compraMapper.toDTOList(compraRepository.findAllByOrderByFechaDesc());
		log.debug("Compras encontradas: {}", compras.size());
		return compras;
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public CompraDTO obtenerPorId(Long id) {
		log.debug("Obteniendo compra id={}", id);
		return compraMapper.toDTO(obtenerEntidad(id));
	}

	@Transactional(readOnly = true)
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public List<CompraDetalleDTO> listarDetalles(Long compraId) {
		log.debug("Listando detalles compraId={}", compraId);
		obtenerEntidad(compraId);
		return detalleMapper.toDTOList(detalleRepository.findByCompraId(compraId));
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public CompraDTO crear(CompraDTO dto) {
		log.debug("Creando compra proveedorId={}", dto.getProveedorId());
		Proveedor proveedor = resolverProveedor(dto.getProveedorId());

		Compra compra = new Compra();
		compra.setFecha(LocalDateTime.now());
		compra.setProveedor(proveedor);
		compra.setEstado(EstadoCompra.BORRADOR);
		compra.setTotal(BigDecimal.ZERO);
		compra.setObservaciones(dto.getObservaciones());
		CompraDTO respuesta = compraMapper.toDTO(compraRepository.save(compra));
		log.info("Compra creada: id={} proveedorId={}", respuesta.getId(), proveedor.getId());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public CompraDTO actualizar(Long id, CompraDTO dto) {
		log.debug("Actualizando compra id={}", id);
		Compra compra = obtenerEntidad(id);
		validarBorrador(compra);
		Proveedor proveedor = resolverProveedor(dto.getProveedorId());
		compra.setProveedor(proveedor);
		compra.setObservaciones(dto.getObservaciones());
		CompraDTO respuesta = compraMapper.toDTO(compraRepository.save(compra));
		log.info("Compra actualizada: id={} proveedorId={}", respuesta.getId(), proveedor.getId());
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public CompraDetalleDTO agregarDetalle(CompraDetalleDTO dto) {
		log.debug("Agregando detalle compraId={} tipo={}", dto.getCompraId(), dto.getTipo());
		ValidacionDatos.requerido(dto.getCompraId(), "La compra");
		Compra compra = obtenerEntidad(dto.getCompraId());
		validarBorrador(compra);
		TipoItem tipo = dto.getTipo();
		ValidacionDatos.requerido(tipo, "El tipo de ítem");
		BigDecimal cantidad = ValidacionDatos.stock(dto.getCantidad(), "La cantidad");
		if (cantidad.signum() <= 0) {
			log.debug("Detalle rechazado, cantidad no positiva");
			throw new IllegalArgumentException("La cantidad: debe ser mayor a cero");
		}
		BigDecimal precioUnitario = ValidacionDatos.stock(dto.getPrecioUnitario(), "El precio de compra");

		CompraDetalle detalle = detalleMapper.toEntity(dto);
		detalle.setCompra(compra);
		detalle.setTipo(tipo);
		detalle.setCantidad(cantidad);
		detalle.setPrecioUnitario(precioUnitario);

		if (tipo == TipoItem.INGREDIENTE) {
			ValidacionDatos.requerido(dto.getIngredienteId(), "El ingrediente");
			Ingrediente ingrediente = ingredienteRepository.findById(dto.getIngredienteId())
					.orElseThrow(() -> new NoSuchElementException(
							"Ingrediente no encontrado: " + dto.getIngredienteId()));
			if (!ingrediente.isActivo()) {
				log.debug("Detalle rechazado, ingrediente inactivo id={}", ingrediente.getId());
				throw new IllegalArgumentException("El ingrediente está inactivo");
			}
			if (detalleRepository.existsByCompraIdAndIngredienteId(compra.getId(), ingrediente.getId())) {
				log.debug("Detalle rechazado, ingrediente duplicado en compra");
				throw new IllegalArgumentException("El ingrediente ya está en la compra");
			}
			detalle.setIngrediente(ingrediente);
		} else {
			ValidacionDatos.requerido(dto.getProductoId(), "El producto");
			Producto producto = productoRepository.findById(dto.getProductoId())
					.orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + dto.getProductoId()));
			if (!producto.isActivo()) {
				log.debug("Detalle rechazado, producto inactivo id={}", producto.getId());
				throw new IllegalArgumentException("El producto está inactivo");
			}
			if (detalleRepository.existsByCompraIdAndProductoId(compra.getId(), producto.getId())) {
				log.debug("Detalle rechazado, producto duplicado en compra");
				throw new IllegalArgumentException("El producto ya está en la compra");
			}
			detalle.setProducto(producto);
		}
		detalle.setSubtotal(cantidad.multiply(precioUnitario));
		CompraDetalleDTO respuesta = detalleMapper.toDTO(detalleRepository.save(detalle));
		recalcularTotal(compra.getId());
		log.info("Detalle agregado: compraId={} tipo={} cantidad={} precioUnitario={}", compra.getId(), tipo,
				cantidad, precioUnitario);
		return respuesta;
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public void eliminarDetalle(Long detalleId) {
		log.debug("Eliminando detalle id={}", detalleId);
		CompraDetalle detalle = detalleRepository.findById(detalleId)
				.orElseThrow(() -> new NoSuchElementException("Detalle no encontrado: " + detalleId));
		validarBorrador(detalle.getCompra());
		Long compraId = detalle.getCompra().getId();
		detalleRepository.delete(detalle);
		recalcularTotal(compraId);
		log.info("Detalle eliminado: id={} compraId={}", detalleId, compraId);
	}

	@Transactional
	@PreAuthorize("hasAnyRole('DEV', 'ADMIN')")
	public CompraDTO confirmar(Long id) {
		log.debug("Confirmando compra id={}", id);
		Compra compra = obtenerEntidad(id);
		if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
			log.debug("Confirmación rechazada, compra ya confirmada id={}", id);
			throw new IllegalArgumentException("La compra ya fue confirmada");
		}
		List<CompraDetalle> detalles = detalleRepository.findByCompraId(compra.getId());
		if (detalles.isEmpty()) {
			log.debug("Confirmación rechazada, compra sin líneas id={}", id);
			throw new IllegalArgumentException("La compra no tiene líneas para confirmar");
		}

		List<Long> ingredientesAfectados = new ArrayList<>();
		for (CompraDetalle detalle : detalles) {
			if (detalle.getTipo() == TipoItem.INGREDIENTE) {
				ingredienteService.ingresarStock(detalle.getIngrediente().getId(), detalle.getCantidad(),
						detalle.getPrecioUnitario());
				ingredientesAfectados.add(detalle.getIngrediente().getId());
			} else {
				productoService.ingresarStock(detalle.getProducto().getId(), detalle.getCantidad(),
						detalle.getPrecioUnitario());
			}
		}
		for (Long ingredienteId : ingredientesAfectados) {
			recetaService.recalcularCostosPorIngrediente(ingredienteId);
		}

		compra.setEstado(EstadoCompra.CONFIRMADA);
		CompraDTO respuesta = compraMapper.toDTO(compraRepository.save(compra));
		log.info("Compra confirmada: id={} proveedorId={} lineas={}", compra.getId(),
				compra.getProveedor().getId(), detalles.size());
		return respuesta;
	}

	private Compra obtenerEntidad(Long id) {
		return compraRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("Compra no encontrada: " + id));
	}

	private void validarBorrador(Compra compra) {
		if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
			log.debug("Operación rechazada, compra confirmada id={}", compra.getId());
			throw new IllegalArgumentException("La compra ya fue confirmada y no se puede modificar");
		}
	}

	private Proveedor resolverProveedor(Long proveedorId) {
		if (proveedorId == null) {
			return obtenerProveedorPorDefecto();
		}
		return proveedorRepository.findById(proveedorId)
				.orElseThrow(() -> new NoSuchElementException("Proveedor no encontrado: " + proveedorId));
	}

	private Proveedor obtenerProveedorPorDefecto() {
		return proveedorRepository.findByDniNitIgnoreCase(DNI_NIT_PROVEEDOR_POR_DEFECTO).orElseGet(() -> {
			Proveedor proveedor = new Proveedor();
			proveedor.setNombre(NOMBRE_PROVEEDOR_POR_DEFECTO);
			proveedor.setDniNit(DNI_NIT_PROVEEDOR_POR_DEFECTO);
			proveedor.setActivo(true);
			Proveedor creado = proveedorRepository.save(proveedor);
			log.info("Proveedor por defecto creado: id={} dniNit={}", creado.getId(), DNI_NIT_PROVEEDOR_POR_DEFECTO);
			return creado;
		});
	}

	private void recalcularTotal(Long compraId) {
		BigDecimal total = BigDecimal.ZERO;
		for (CompraDetalle detalle : detalleRepository.findByCompraId(compraId)) {
			total = total.add(detalle.getSubtotal());
		}
		Compra compra = obtenerEntidad(compraId);
		compra.setTotal(total);
		compraRepository.save(compra);
		log.debug("Total recalculado compraId={} total={}", compraId, total);
	}

}
