package com.mibombay.empresa.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mibombay.empresa.dto.CompraDTO;
import com.mibombay.empresa.model.Compra;

@Mapper(componentModel = "spring")
public interface CompraMapper {

	@Mapping(target = "fecha", expression = "java(formatearFecha(compra.getFecha()))")
	@Mapping(target = "proveedorId", source = "proveedor.id")
	@Mapping(target = "proveedorNombre", source = "proveedor.nombre")
	CompraDTO toDTO(Compra compra);

	List<CompraDTO> toDTOList(List<Compra> compras);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "fecha", ignore = true)
	@Mapping(target = "proveedor", ignore = true)
	@Mapping(target = "estado", ignore = true)
	@Mapping(target = "total", ignore = true)
	Compra toEntity(CompraDTO dto);

	default String formatearFecha(LocalDateTime fecha) {
		return fecha == null ? "" : DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(fecha);
	}

}
