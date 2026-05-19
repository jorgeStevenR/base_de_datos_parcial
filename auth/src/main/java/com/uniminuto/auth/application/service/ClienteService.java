package com.uniminuto.auth.application.service;

import com.uniminuto.auth.application.dto.request.ClienteRequest;
import com.uniminuto.auth.domain.exception.ConflictException;
import com.uniminuto.auth.domain.exception.ResourceNotFoundException;
import com.uniminuto.auth.domain.model.Cliente;
import com.uniminuto.auth.infrastructure.persistence.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

    // =========================
    // GENERAR PDF DEL CLIENTE
    // =========================
    public File generarClientePdf(Long id) throws IOException {
        Cliente cliente = buscarPorId(id);

        // Crear directorio si no existe
        File directorio = new File("uploads");
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        String nombreArchivo = "cliente_" + cliente.getId() + "_"
                + cliente.getDocumentNumber() + "_" + System.currentTimeMillis() + ".pdf";
        String rutaCompleta = "uploads" + File.separator + nombreArchivo;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                int y = 750;
                int marginLeft = 50;

                // TÍTULO
                contentStream.setFont(fontBold, 20);
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("INFORMACIÓN DEL CLIENTE");
                contentStream.endText();

                y -= 40;

                // DATOS DEL CLIENTE
                contentStream.setFont(fontBold, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Datos Personales");
                contentStream.endText();

                y -= 25;

                contentStream.setFont(fontNormal, 11);

                // Número de cédula
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Cédula: " + cliente.getDocumentNumber());
                contentStream.endText();
                y -= 20;

                // Nombres
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Nombre: " + cliente.getFirstName() + " " + cliente.getLastName());
                contentStream.endText();
                y -= 20;

                // Email
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Email: " + cliente.getEmail());
                contentStream.endText();
                y -= 20;

                // Teléfono
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Teléfono: " + cliente.getPhone());
                contentStream.endText();
                y -= 20;

                // Dirección
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Dirección: " + cliente.getAddress());
                contentStream.endText();
                y -= 20;

                // Fecha de registro
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Registrado: " 
                        + cliente.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                contentStream.endText();

                y -= 40;

                // LÍNEA SEPARADORA
                contentStream.setLineWidth(1f);
                contentStream.moveTo(marginLeft, y);
                contentStream.lineTo(550, y);
                contentStream.stroke();

                y -= 30;

                // INFORMACIÓN ADICIONAL
                contentStream.setFont(fontBold, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("RUT Registrado");
                contentStream.endText();

                y -= 25;

                contentStream.setFont(fontNormal, 11);
                if (cliente.getRutPdfNombre() != null && !cliente.getRutPdfNombre().isEmpty()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(marginLeft, y);
                    contentStream.showText("Archivo: " + cliente.getRutPdfNombre());
                    contentStream.endText();
                } else {
                    contentStream.setFont(fontNormal, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(marginLeft, y);
                    contentStream.showText("(No hay RUT registrado)");
                    contentStream.endText();
                }

                y -= 30;

                // FOOTER
                contentStream.setFont(fontNormal, 9);
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, 30);
                contentStream.showText("Documento generado automáticamente - " 
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                contentStream.endText();
            }

            document.save(rutaCompleta);
            log.info("PDF de cliente generado: {}", rutaCompleta);
        }

        return new File(rutaCompleta);
    }

    // =========================
    // ELIMINAR CLIENTE
    // =========================
    @Transactional
    public void eliminarCliente(Long id) {
        Cliente cliente = buscarPorId(id);
        clienteRepository.delete(cliente);
        log.info("Cliente eliminado: ID={}, Cédula={}", id, cliente.getDocumentNumber());
    }
}