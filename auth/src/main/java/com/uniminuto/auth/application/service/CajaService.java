package com.uniminuto.auth.application.service;

import com.uniminuto.auth.application.dto.response.CajaResponse;
import com.uniminuto.auth.application.dto.response.VentaResponse;
import com.uniminuto.auth.domain.model.Venta;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CajaService implements com.uniminuto.auth.domain.port.in.CajaServicePort {

    private final VentaRepository ventaRepository;

    // =========================
    // OBTENER SALDO EN CAJA
    // =========================
    @Transactional(readOnly = true)
    public CajaResponse obtenerSaldo() {
        List<Venta> ventas = ventaRepository.findAllByOrderByFechaVentaDesc();

        double saldoTotal = ventas.stream()
                .mapToDouble(Venta::getTotal)
                .sum();

        List<VentaResponse> historico = ventas.stream()
                .map(this::mapToResponse)
                .toList();

        return CajaResponse.builder()
                .saldoTotal(saldoTotal)
                .totalVentas(ventas.size())
                .historico(historico)
                .build();
    }

    // =========================
    // DESCARGAR HISTÓRICO PDF
    // =========================
    public File generarHistoricoPdf() throws IOException {
        List<Venta> ventas = ventaRepository.findAllByOrderByFechaVentaDesc();

        double saldoTotal = ventas.stream()
                .mapToDouble(Venta::getTotal)
                .sum();

        String historicosDir = "historicos";
        File directorio = new File(historicosDir);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        String nombreArchivo = "historico_ventas_" + System.currentTimeMillis() + ".pdf";
        String rutaCompleta = historicosDir + File.separator + nombreArchivo;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                int y = 750;
                int marginLeft = 50;

                // TÍTULO
                contentStream.setFont(fontBold, 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("HISTÓRICO DE VENTAS");
                contentStream.endText();

                y -= 10;
                contentStream.setFont(fontNormal, 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Generado: "
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                contentStream.endText();

                y -= 30;

                // ENCABEZADO TABLA
                contentStream.setFont(fontBold, 10);
                contentStream.setLineWidth(1f);
                contentStream.moveTo(marginLeft, y);
                contentStream.lineTo(550, y);
                contentStream.stroke();

                y -= 5;
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft + 5, y);
                contentStream.showText("Factura #");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(100, y);
                contentStream.showText("Cliente");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(300, y);
                contentStream.showText("Fecha");
                contentStream.endText();

                contentStream.beginText();
                contentStream.newLineAtOffset(450, y);
                contentStream.showText("Total");
                contentStream.endText();

                y -= 5;
                contentStream.moveTo(marginLeft, y);
                contentStream.lineTo(550, y);
                contentStream.stroke();

                y -= 18;

                // DATOS
                contentStream.setFont(fontNormal, 9);
                for (Venta venta : ventas) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(marginLeft + 5, y);
                    contentStream.showText(String.valueOf(venta.getId()));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(100, y);
                    contentStream.showText(truncate(venta.getClienteNombre(), 28));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(300, y);
                    contentStream.showText(venta.getFechaVenta()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(450, y);
                    contentStream.showText("$" + String.format("%.2f", venta.getTotal()));
                    contentStream.endText();

                    y -= 16;

                    // Si se acaba la página, salimos
                    if (y < 80) break;
                }

                y -= 30;
                contentStream.moveTo(marginLeft, y);
                contentStream.lineTo(550, y);
                contentStream.stroke();

                y -= 25;

                // TOTAL ACUMULADO
                contentStream.setFont(fontBold, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("SALDO TOTAL EN CAJA: $" + String.format("%.2f", saldoTotal));
                contentStream.endText();

                y -= 20;
                contentStream.setFont(fontNormal, 11);
                contentStream.beginText();
                contentStream.newLineAtOffset(marginLeft, y);
                contentStream.showText("Total de ventas realizadas: " + ventas.size());
                contentStream.endText();
            }

            document.save(rutaCompleta);
        }

        log.info("Histórico PDF generado: {}", rutaCompleta);
        return new File(rutaCompleta);
    }

    private VentaResponse mapToResponse(Venta venta) {
        List<VentaResponse.DetalleResponse> detalles = venta.getDetalles().stream()
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
                .detalles(detalles)
                .build();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }
}
