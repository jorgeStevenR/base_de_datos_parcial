package com.uniminuto.auth.application.dto.request;
 
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
 
import java.util.List;
 
@Data
public class VentaRequest {
 
    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;
 
    private String documentNumber;
 
    @NotEmpty(message = "Debe incluir al menos un producto")
    private List<DetalleRequest> detalles;
 
    @Data
    public static class DetalleRequest {
 
        @NotNull(message = "El ID del producto es obligatorio")
        private Long productoId;
 
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;
    }
}