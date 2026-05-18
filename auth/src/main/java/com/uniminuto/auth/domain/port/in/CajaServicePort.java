package com.uniminuto.auth.domain.port.in;

import com.uniminuto.auth.application.dto.response.CajaResponse;

import java.io.File;
import java.io.IOException;

public interface CajaServicePort {

    CajaResponse obtenerSaldo();

    File generarHistoricoPdf() throws IOException;
}
