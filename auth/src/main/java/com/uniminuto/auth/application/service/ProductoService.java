package com.uniminuto.auth.application.service;

import com.uniminuto.auth.application.dto.request.ProductoRequest;
import com.uniminuto.auth.application.dto.response.ProductoResponse;
import com.uniminuto.auth.domain.exception.ResourceNotFoundException;
import com.uniminuto.auth.domain.model.Producto;
import com.uniminuto.auth.domain.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional
    public ProductoResponse crearProducto(ProductoRequest request) {

        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .build();

        productoRepository.save(producto);

        return mapToResponse(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductos() {
        return productoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerProducto(Long id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Producto no encontrado"));

        return mapToResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizarProducto(
            Long id,
            ProductoRequest request
    ) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Producto no encontrado"));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());

        productoRepository.save(producto);

        return mapToResponse(producto);
    }

    @Transactional
    public void eliminarProducto(Long id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Producto no encontrado"));

        productoRepository.delete(producto);
    }

    private ProductoResponse mapToResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .build();
    }
}
