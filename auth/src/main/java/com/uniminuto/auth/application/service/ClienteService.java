package com.uniminuto.auth.application.service;

import com.uniminuto.auth.application.dto.request.ClienteRequest;
import com.uniminuto.auth.domain.model.Cliente;
import com.uniminuto.auth.domain.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente crearCliente(ClienteRequest request, MultipartFile archivo) throws IOException {

        // VALIDAR CÉDULA
        if (clienteRepository.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new RuntimeException("La cédula ya está registrada");
        }

        // VALIDAR EMAIL
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // CREAR CARPETA uploads/ruts SI NO EXISTE
        String uploadDir = System.getProperty("user.dir")
                + File.separator
                + "uploads"
                + File.separator
                + "ruts";

        File directorio = new File(uploadDir);

        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        // GENERAR NOMBRE ÚNICO DEL PDF
        String nombreArchivo = System.currentTimeMillis()
                + "_"
                + archivo.getOriginalFilename();

        // RUTA COMPLETA
        String rutaArchivo = uploadDir
                + File.separator
                + nombreArchivo;

        // GUARDAR ARCHIVO
        archivo.transferTo(new File(rutaArchivo));

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

        // GUARDAR EN BASE DE DATOS
        return clienteRepository.save(cliente);
    }
}