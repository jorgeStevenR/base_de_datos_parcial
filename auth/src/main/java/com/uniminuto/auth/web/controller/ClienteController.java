package com.uniminuto.auth.web.controller;

import com.uniminuto.auth.application.dto.request.ClienteRequest;
import com.uniminuto.auth.domain.port.in.ClienteServicePort;
import com.uniminuto.auth.domain.model.Cliente;
import com.uniminuto.auth.domain.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteServicePort clienteService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Cliente> crearCliente(
            @Valid @RequestPart("cliente") ClienteRequest request,
            @RequestPart(value = "archivo", required = false)
            MultipartFile archivo
    ) throws IOException {

        Cliente clienteCreado = clienteService.crearCliente(request, archivo);

        return ResponseEntity.ok(clienteCreado);
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @GetMapping("/buscar/{documentNumber}")
    public ResponseEntity<Cliente> buscarPorCedula(
            @PathVariable String documentNumber
    ) {
        return ResponseEntity.ok(clienteService.buscarPorCedula(documentNumber));
    }

    // =========================
    // DESCARGAR PDF RUT
    // =========================
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarPorId(id);

        if (cliente.getRutPdfBase64() == null) {
            throw new ResourceNotFoundException("El cliente no tiene PDF asociado");
        }

        byte[] pdfBytes = Base64.getDecoder().decode(cliente.getRutPdfBase64());

        String filename = cliente.getRutPdfNombre() != null
                ? cliente.getRutPdfNombre()
                : "RUT_" + cliente.getDocumentNumber() + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }
}