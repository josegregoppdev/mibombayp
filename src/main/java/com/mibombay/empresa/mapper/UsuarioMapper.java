package com.mibombay.empresa.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mibombay.empresa.dto.UsuarioDTORequest;
import com.mibombay.empresa.dto.UsuarioDTOResponse;
import com.mibombay.empresa.model.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

	UsuarioDTOResponse toResponse(Usuario usuario);

	List<UsuarioDTOResponse> toResponseList(List<Usuario> usuarios);

	Usuario toEntity(UsuarioDTORequest request);

	@Mapping(target = "password", ignore = true)
	UsuarioDTORequest toRequest(UsuarioDTOResponse response);

}
