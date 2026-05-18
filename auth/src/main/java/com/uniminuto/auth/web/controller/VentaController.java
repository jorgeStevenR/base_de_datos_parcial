package com.uniminuto.auth.web.controller;

import com.uniminuto.auth.application.dto.request.VentaRequest;
import com.uniminuto.auth.application.dto.response.VentaResponse;
import com.uniminuto.auth.domain.port.in.VentaServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaServicePort ventaService;

    // =========================
    // CREAR VENTA
    // =========================
    @PostMapping
    public ResponseEntity<VentaResponse> crearVenta(
            @Valid @RequestBody VentaRequest request
    ) throws IOException {

        VentaResponse response = ventaService.crearVenta(request);
        return ResponseEntity.ok(response);
    }

    // =========================
    // LISTAR VENTAS
    // =========================
    @GetMapping
    public ResponseEntity<List<VentaResponse>> listarVentas() {
        return ResponseEntity.ok(ventaService.listarVentas());
    }

    // =========================
    // OBTENER VENTA POR ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerVenta(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ventaService.obtenerVenta(id));
    }

    // =========================
    // DESCARGAR FACTURA PDF
    // =========================
    @GetMapping("/{id}/factura")
    public ResponseEntity<Resource> descargarFactura(
            @PathVariable Long id
    ) {
        java.io.File pdfFile = ventaService.obtenerFacturaPdf(id);

        Resource resource = new FileSystemResource(pdfFile);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + pdfFile.getName() + "\"")
                .body(resource);
    }
}
