package com.mibombay.empresa.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.mibombay.empresa.dto.RecetaDTO;
import com.mibombay.empresa.model.Receta;

@Mapper(componentModel = "spring")
public interface RecetaMapper {

	RecetaDTO toDTO(Receta receta);

	List<RecetaDTO> toDTOList(List<Receta> recetas);

	Receta toEntity(RecetaDTO dto);

}
