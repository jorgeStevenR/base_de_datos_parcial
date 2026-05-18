package com.uniminuto.auth.application.service;

import com.uniminuto.auth.application.dto.request.VentaRequest;
import com.uniminuto.auth.application.dto.response.VentaResponse;
import com.uniminuto.auth.domain.exception.BadRequestException;
import com.uniminuto.auth.domain.exception.ResourceNotFoundException;
import com.uniminuto.auth.domain.model.Cliente;
import com.uniminuto.auth.domain.model.DetalleVenta;
import com.uniminuto.auth.domain.model.Producto;
import com.uniminuto.auth.domain.model.Venta;
import com.uniminuto.auth.infrastructure.persistence.ClienteRepository;
import com.uniminuto.auth.infrastructure.persistence.ProductoRepository;
import com.uniminuto.auth.infrastructure.persistence.VentaRepository;
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

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaService implements com.uniminuto.auth.domain.port.in.VentaServicePort {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    private final String facturasDir = "facturas";

    // =========================
    // CREAR VENTA
    // =========================
    @Transactional
    public VentaResponse crearVenta(VentaRequest request) throws IOException {

        // 1. BUSCAR CLIENTE
        Cliente cliente = null;
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        } else if (request.getDocumentNumber() != null && !request.getDocumentNumber().isBlank()) {
            cliente = clienteRepository.findByDocumentNumber(request.getDocumentNumber())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado con cédula: " + request.getDocumentNumber()
                    ));
        }

        String clienteNombre = (cliente != null)
                ? cliente.getFirstName() + " " + cliente.getLastName()
                : "Consumidor Final";
        String clienteDocumento = (cliente != null) ? cliente.getDocumentNumber() : "N/A";

        // 2. CREAR VENTA
        Venta venta = Venta.builder()
                .clienteNombre(clienteNombre)
                .clienteDocumento(clienteDocumento)
                .fechaVenta(LocalDateTime.now())
                .total(0.0)
                .build();

        List<DetalleVenta> detalles = new ArrayList<>();
        double total = 0.0;

        for (VentaRequest.DetalleRequest detalleReq : request.getDetalles()) {

            Producto producto = productoRepository.findById(detalleReq.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado: " + detalleReq.getProductoId()
                    ));

            // Validar stock
            if (producto.getStock() < detalleReq.getCantidad()) {
                throw new BadRequestException(
                        "Stock insuficiente para '" + producto.getNombre()
                                + "'. Disponible: " + producto.getStock()
                                + ", solicitado: " + detalleReq.getCantidad()
                );
            }

            // Descontar stock
            producto.setStock(producto.getStock() - detalleReq.getCantidad());
            productoRepository.save(producto);

            double subtotal = producto.getPrecio() * detalleReq.getCantidad();

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .productoId(producto.getId())
                    .productoNombre(producto.getNombre())
                    .cantidad(detalleReq.getCantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .build();

            detalles.add(detalle);
            total += subtotal;
        }

        venta.setDetalles(detalles);
        venta.setTotal(total);

        // Guardar para obtener ID
        venta = ventaRepository.save(venta);

        // 3. GENERAR FACTURA PDF
        String facturaPath = generarFacturaPdf(venta);
        venta.setFacturaPdfPath(facturaPath);
        ventaRepository.save(venta);

        log.info("Venta creada: ID={}, Total={}, Cliente={}", venta.getId(), total, clienteNombre);

        return mapToResponse(venta);
    }

    // =========================
    // LISTAR VENTAS
    // =========================
    @Transactional(readOnly = true)
    public List<VentaResponse> listarVentas() {
        return ventaRepository.findAllByOrderByFechaVentaDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================
    // OBTENER VENTA POR ID
    // =========================
    @Transactional(readOnly = true)
    public VentaResponse obtenerVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));
        return mapToResponse(venta);
    }

    // =========================
    // OBTENER FACTURA PDF
    // =========================
    public File obtenerFacturaPdf(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada"));

        if (venta.getFacturaPdfPath() == null) {
            throw new ResourceNotFoundException("La factura PDF no existe para esta venta");
        }

        File pdfFile = new File(venta.getFacturaPdfPath());
        if (!pdfFile.exists()) {
            throw new ResourceNotFoundException("El archivo PDF no se encuentra en el servidor");
        }

        return pdfFile;
    }

    // =========================
    // MAP TO RESPONSE
    // =========================
    private VentaResponse mapToResponse(Venta venta) {
        List<VentaResponse.DetalleResponse> detallesResponse = venta.getDetalles().stream()
                .map(d -> VentaResponse.DetalleResponse.builder()
                        .productoId(d.getProductoId())
                        .productoNombre(d.getProductoNombre())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotal(d.getSubtotal())
                        .build())
                .toList();

        return VentaResponse.builder()
                .id(venta.getId())
                .clienteNombre(venta.getClienteNombre())
                .clienteDocumento(venta.getClienteDocumento())
                .total(venta.getTotal())
                .fechaVenta(venta.getFechaVenta())
                .detalles(detallesResponse)
                .build();
    }

    // =========================
    // GENERAR FACTURA PDF
    // =========================
    private String generarFacturaPdf(Venta venta) throws IOException {

        // Crear directorio si no existe
        File directorio = new File(facturasDir);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        String nombreArchivo = "factura_" + venta.getId() + "_"
                + System.currentTimeMillis() + ".pdf";
        String rutaCompleta = facturasDir + File.separator + nombreArchivo;

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
                contentStream.showText("FACTURA DE VENTA");
                contentStream.endText();

                y -= 30;

                // DATOS DE LA FACTURA
                contentStream.setFont(fontBold, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Factura #: " + venta.getId());
                contentStream.endText();

                y -= 20;

                contentStream.setFont(fontNormal, 11);
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Fecha: "
                        + venta.getFechaVenta().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                contentStream.endText();

                y -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Cliente: " + venta.getClienteNombre());
                contentStream.endText();

                y -= 20;
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Documento: " + venta.getClienteDocumento());
                contentStream.endText();

                y -= 40;

                // ENCABEZADO TABLA
                contentStream.setFont(fontBold, 11);
                contentStream.setLineWidth(1f);
                contentStream.moveTo(marginLeft, y);
                contentStream.lineTo(550, y);
                contentStream.stroke();

                y -= 5;
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft + 5, y);
                contentStream.showText("Producto");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(300, y);
                contentStream.showText("Cant.");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(370, y);
                contentStream.showText("Precio");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(450, y);
                contentStream.showText("Subtotal");
                contentStream.endText();

                y -= 5;
                contentStream.moveTo(marginLeft, y);
                contentStream.lineTo(550, y);
                contentStream.stroke();

                y -= 20;

                // DETALLES
                contentStream.setFont(fontNormal, 10);
                for (DetalleVenta detalle : venta.getDetalles()) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(marginLeft + 5, y);
                    contentStream.showText(truncate(detalle.getProductoNombre(), 35));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(310, y);
                    contentStream.showText(String.valueOf(detalle.getCantidad()));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(370, y);
                    contentStream.showText("$" + String.format("%.2f", detalle.getPrecioUnitario()));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(450, y);
                    contentStream.showText("$" + String.format("%.2f", detalle.getSubtotal()));
                    contentStream.endText();

                    y -= 20;
                }

                // LÍNEA FINAL
                contentStream.moveTo(marginLeft, y);
                contentStream.lineTo(550, y);
                contentStream.stroke();

                y -= 25;

                // TOTAL
                contentStream.setFont(fontBold, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(380, y);
                contentStream.showText("TOTAL: $" + String.format("%.2f", venta.getTotal()));
                contentStream.endText();
            }
        }

        log.info("Factura PDF generada: {}", rutaCompleta);
        return rutaCompleta;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
