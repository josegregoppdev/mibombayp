package com.mibombay.empresa.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mibombay.empresa.dto.DetalleRecetaDTO;
import com.mibombay.empresa.model.DetalleReceta;

@Mapper(componentModel = "spring")
public interface DetalleRecetaMapper {

	@Mapping(target = "recetaId", source = "receta.id")
	@Mapping(target = "ingredienteId", source = "ingrediente.id")
	@Mapping(target = "ingredienteNombre", source = "ingrediente.nombre")
	DetalleRecetaDTO toDTO(DetalleReceta detalle);

	List<DetalleRecetaDTO> toDTOList(List<DetalleReceta> detalles);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "receta", ignore = true)
	@Mapping(target = "ingrediente", ignore = true)
	DetalleReceta toEntity(DetalleRecetaDTO dto);

}
