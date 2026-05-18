package com.uniminuto.auth.domain.port.in;

import com.uniminuto.auth.application.dto.request.VentaRequest;
import com.uniminuto.auth.application.dto.response.VentaResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface VentaServicePort {

    VentaResponse crearVenta(VentaRequest request) throws IOException;

    List<VentaResponse> listarVentas();

    VentaResponse obtenerVenta(Long id);

    File obtenerFacturaPdf(Long ventaId);
}
