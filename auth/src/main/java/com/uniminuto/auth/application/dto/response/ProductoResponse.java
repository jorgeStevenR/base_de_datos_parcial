package com.uniminuto.auth.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductoResponse {

    private Long id;

    private String nombre;

    private String descripcion;

    private Double precio;

    private Integer stock;
}