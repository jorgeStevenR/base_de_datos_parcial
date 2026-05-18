package com.uniminuto.auth.web.controller;

import com.uniminuto.auth.application.dto.request.ProductoRequest;
import com.uniminuto.auth.application.dto.response.ProductoResponse;
import com.uniminuto.auth.domain.port.in.ProductoServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoServicePort productoService;

    @PostMapping
    public ResponseEntity<ProductoResponse> crearProducto(
            @Valid @RequestBody ProductoRequest request
    ) {
        return ResponseEntity.ok(
                productoService.crearProducto(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarProductos() {
        return ResponseEntity.ok(
                productoService.listarProductos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtenerProducto(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                productoService.obtenerProducto(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request
    ) {
        return ResponseEntity.ok(
                productoService.actualizarProducto(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable Long id
    ) {

        productoService.eliminarProducto(id);

        return ResponseEntity.noContent().build();
    }
}