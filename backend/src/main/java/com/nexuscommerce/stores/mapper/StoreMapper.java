package com.nexuscommerce.stores.mapper;

import com.nexuscommerce.stores.api.dto.StoreResponse;
import com.nexuscommerce.stores.domain.Store;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    StoreResponse toResponse(Store store);
}
