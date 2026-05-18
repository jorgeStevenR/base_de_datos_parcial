package com.uniminuto.auth.application.dto.response;
 
import lombok.Builder;
import lombok.Data;
 
import java.util.List;
 
@Data
@Builder
public class CajaResponse {
 
    private Double saldoTotal;
    private Integer totalVentas;
    private List<VentaResponse> historico;
}