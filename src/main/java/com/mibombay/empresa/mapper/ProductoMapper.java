package com.mibombay.empresa.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.mibombay.empresa.dto.ProductoDTO;
import com.mibombay.empresa.model.Producto;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

	ProductoDTO toDTO(Producto producto);

	List<ProductoDTO> toDTOList(List<Producto> productos);

	Producto toEntity(ProductoDTO dto);

}
