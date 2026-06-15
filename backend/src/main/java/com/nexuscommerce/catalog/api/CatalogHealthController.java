package com.nexuscommerce.catalog.api;

import com.nexuscommerce.catalog.api.dto.CatalogHealthResponse;
import com.nexuscommerce.catalog.service.CatalogHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogHealthController {

    private final CatalogHealthService catalogHealthService;

    @GetMapping("/health")
    public CatalogHealthResponse health() {
        return catalogHealthService.probe();
    }
}
