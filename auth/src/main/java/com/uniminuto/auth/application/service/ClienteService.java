package com.uniminuto.auth.application.service;

import com.uniminuto.auth.application.dto.request.ClienteRequest;
import com.uniminuto.auth.domain.exception.ConflictException;
import com.uniminuto.auth.domain.exception.ResourceNotFoundException;
import com.uniminuto.auth.domain.model.Cliente;
import com.uniminuto.auth.domain.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    private final String uploadDir = "uploads/ruts";

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

        // CREAR CARPETA SI NO EXISTE
        File directorio = new File(uploadDir);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        String rutaArchivo = null;

        // SUBIR PDF
        if (archivo != null && !archivo.isEmpty()) {

            String nombreArchivo = System.currentTimeMillis()
                    + "_"
                    + archivo.getOriginalFilename();

            rutaArchivo = uploadDir + File.separator + nombreArchivo;

            archivo.transferTo(new File(rutaArchivo));
        }

        // CREAR CLIENTE
        Cliente cliente = Cliente.builder()
                .documentNumber(request.getDocumentNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .rutPdfPath(rutaArchivo)
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
}