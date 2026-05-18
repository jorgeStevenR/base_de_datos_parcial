package com.uniminuto.auth.domain.port.in;

import com.uniminuto.auth.application.dto.request.ProductoRequest;
import com.uniminuto.auth.application.dto.response.ProductoResponse;

import java.util.List;

public interface ProductoServicePort {

    ProductoResponse crearProducto(ProductoRequest request);

    List<ProductoResponse> listarProductos();

    ProductoResponse obtenerProducto(Long id);

    ProductoResponse actualizarProducto(Long id, ProductoRequest request);

    void eliminarProducto(Long id);
}
