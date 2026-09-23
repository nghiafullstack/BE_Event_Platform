package org.example.eventplatform.event.controller;

import lombok.RequiredArgsConstructor;
import org.example.eventplatform.event.dto.PublicPackageResponse;
import org.example.eventplatform.event.dto.PublicTroupeResponse;
import org.example.eventplatform.event.service.PublicCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Không yêu cầu đăng nhập — khách vãng lai duyệt sàn trước khi tạo tài khoản. */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final PublicCatalogService publicCatalogService;

    @GetMapping("/troupes")
    public ResponseEntity<List<PublicTroupeResponse>> listTroupes() {
        return ResponseEntity.ok(publicCatalogService.listTroupes());
    }

    @GetMapping("/troupes/{id}")
    public ResponseEntity<PublicTroupeResponse> getTroupe(@PathVariable Long id) {
        return ResponseEntity.ok(publicCatalogService.getTroupe(id));
    }

    @GetMapping("/packages")
    public ResponseEntity<List<PublicPackageResponse>> listPackages() {
        return ResponseEntity.ok(publicCatalogService.listPackages());
    }
}
