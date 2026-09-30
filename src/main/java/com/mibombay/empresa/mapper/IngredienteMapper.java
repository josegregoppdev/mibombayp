package com.mibombay.empresa.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.mibombay.empresa.dto.IngredienteDTO;
import com.mibombay.empresa.model.Ingrediente;

@Mapper(componentModel = "spring")
public interface IngredienteMapper {

	IngredienteDTO toDTO(Ingrediente ingrediente);

	List<IngredienteDTO> toDTOList(List<Ingrediente> ingredientes);

	Ingrediente toEntity(IngredienteDTO dto);

}
