package com.uniminuto.auth.web.controller;

import com.uniminuto.auth.application.dto.response.CajaResponse;
import com.uniminuto.auth.domain.port.in.CajaServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaServicePort cajaService;

    // =========================
    // OBTENER SALDO EN CAJA
    // =========================
    @GetMapping("/saldo")
    public ResponseEntity<CajaResponse> obtenerSaldo() {
        return ResponseEntity.ok(cajaService.obtenerSaldo());
    }

    // =========================
    // DESCARGAR HISTÓRICO PDF
    // =========================
    @GetMapping("/historico/pdf")
    public ResponseEntity<Resource> descargarHistorico() throws IOException {
        java.io.File pdfFile = cajaService.generarHistoricoPdf();

        Resource resource = new FileSystemResource(pdfFile);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + pdfFile.getName() + "\"")
                .body(resource);
    }
}
