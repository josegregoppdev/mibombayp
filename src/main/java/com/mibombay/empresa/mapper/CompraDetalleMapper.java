package com.mibombay.empresa.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mibombay.empresa.dto.CompraDetalleDTO;
import com.mibombay.empresa.model.CompraDetalle;

@Mapper(componentModel = "spring")
public interface CompraDetalleMapper {

	@Mapping(target = "compraId", source = "compra.id")
	@Mapping(target = "ingredienteId", source = "ingrediente.id")
	@Mapping(target = "productoId", source = "producto.id")
	@Mapping(target = "ingredienteNombre", source = "ingrediente.nombre")
	@Mapping(target = "productoNombre", source = "producto.nombre")
	@Mapping(target = "unidadMedida", source = "ingrediente.unidadMedida")
	CompraDetalleDTO toDTO(CompraDetalle detalle);

	List<CompraDetalleDTO> toDTOList(List<CompraDetalle> detalles);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "compra", ignore = true)
	@Mapping(target = "ingrediente", ignore = true)
	@Mapping(target = "producto", ignore = true)
	@Mapping(target = "subtotal", ignore = true)
	CompraDetalle toEntity(CompraDetalleDTO dto);

}
