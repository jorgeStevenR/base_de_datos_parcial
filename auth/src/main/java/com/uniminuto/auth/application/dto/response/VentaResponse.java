package com.uniminuto.auth.application.dto.response;
 
import lombok.Builder;
import lombok.Data;
 
import java.time.LocalDateTime;
import java.util.List;
 
@Data
@Builder
public class VentaResponse {
 
    private Long id;
    private String clienteNombre;
    private String clienteDocumento;
    private Double total;
    private LocalDateTime fechaVenta;
    private List<DetalleResponse> detalles;
 
    @Data
    @Builder
    public static class DetalleResponse {
        private Long productoId;
        private String productoNombre;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}