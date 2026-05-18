package com.uniminuto.auth.web.controller;

import com.uniminuto.auth.application.dto.request.ClienteRequest;
import com.uniminuto.auth.application.service.ClienteService;
import com.uniminuto.auth.domain.model.Cliente;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

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
}