package com.mibombay.empresa.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.mibombay.empresa.dto.ProveedorDTO;
import com.mibombay.empresa.model.Proveedor;

@Mapper(componentModel = "spring")
public interface ProveedorMapper {

	ProveedorDTO toDTO(Proveedor proveedor);

	List<ProveedorDTO> toDTOList(List<Proveedor> proveedores);

	Proveedor toEntity(ProveedorDTO dto);

}
