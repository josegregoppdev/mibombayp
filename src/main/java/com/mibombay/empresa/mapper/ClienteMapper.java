package com.mibombay.empresa.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.mibombay.empresa.dto.ClienteDTO;
import com.mibombay.empresa.model.Cliente;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

	ClienteDTO toDTO(Cliente cliente);

	List<ClienteDTO> toDTOList(List<Cliente> clientes);

	Cliente toEntity(ClienteDTO dto);

}
