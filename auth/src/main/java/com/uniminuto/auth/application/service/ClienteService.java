package com.uniminuto.auth.application.service;

import com.uniminuto.auth.application.dto.request.ClienteRequest;
import com.uniminuto.auth.domain.exception.ConflictException;
import com.uniminuto.auth.domain.exception.ResourceNotFoundException;
import com.uniminuto.auth.domain.model.Cliente;
import com.uniminuto.auth.infrastructure.persistence.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService implements com.uniminuto.auth.domain.port.in.ClienteServicePort {

    private final ClienteRepository clienteRepository;

    // =========================
    // CREAR CLIENTE
    // =========================
    @Transactional
    public Cliente crearCliente(ClienteRequest request, MultipartFile archivo) throws IOException {

        // VALIDAR CÉDULA
        if (clienteRepository.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new ConflictException("La cédula ya está registrada");
        }

        // VALIDAR EMAIL
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El correo ya está registrado");
        }

        String pdfBase64 = null;
        String pdfNombre = null;

        // CONVERTIR PDF A BASE64
        if (archivo != null && !archivo.isEmpty()) {
            pdfNombre = archivo.getOriginalFilename();
            byte[] bytes = archivo.getBytes();
            pdfBase64 = Base64.getEncoder().encodeToString(bytes);
        }

        // CREAR CLIENTE
        Cliente cliente = Cliente.builder()
                .documentNumber(request.getDocumentNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .rutPdfBase64(pdfBase64)
                .rutPdfNombre(pdfNombre)
                .createdAt(LocalDateTime.now())
                .build();

        // GUARDAR EN BD
        return clienteRepository.save(cliente);
    }

    // =========================
    // LISTAR CLIENTES
    // =========================
    @Transactional(readOnly = true)
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    // =========================
    // BUSCAR POR CÉDULA
    // =========================
    @Transactional(readOnly = true)
    public Cliente buscarPorCedula(String documentNumber) {
        return clienteRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cliente no encontrado con cédula: " + documentNumber
                        )
                );
    }

    // =========================
    // BUSCAR POR ID
    // =========================
    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente no encontrado con ID: " + id)
                );
    }
}