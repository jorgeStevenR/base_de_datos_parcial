package com.uniminuto.auth.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @Column(name = "cliente_documento")
    private String clienteDocumento;

    @Column(nullable = false)
    private Double total;

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta;

    @Column(name = "factura_pdf_path")
    private String facturaPdfPath;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleVenta> detalles;
}
