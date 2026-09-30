package com.mibombay.empresa.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mibombay.empresa.dto.ProductoConRecetaDTO;
import com.mibombay.empresa.model.ProductoConReceta;

@Mapper(componentModel = "spring")
public interface ProductoConRecetaMapper {

	@Mapping(target = "recetaId", source = "receta.id")
	@Mapping(target = "recetaNombre", source = "receta.nombre")
	@Mapping(target = "recetaCosto", source = "receta.costo")
	ProductoConRecetaDTO toDTO(ProductoConReceta producto);

	List<ProductoConRecetaDTO> toDTOList(List<ProductoConReceta> productos);

	@Mapping(target = "receta", ignore = true)
	ProductoConReceta toEntity(ProductoConRecetaDTO dto);

}
