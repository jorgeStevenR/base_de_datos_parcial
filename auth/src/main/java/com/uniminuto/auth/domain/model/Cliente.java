package com.uniminuto.auth.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    // DOCUMENTO
    // =========================
    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    // =========================
    // NOMBRES
    // =========================
    @Column(name = "first_name", nullable = false)
    private String firstName;

    // =========================
    // APELLIDOS
    // =========================
    @Column(name = "last_name", nullable = false)
    private String lastName;

    // =========================
    // CORREO
    // =========================
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // =========================
    // TELÉFONO
    // =========================
    @Column(name = "phone", nullable = false)
    private String phone;

    // =========================
    // DIRECCIÓN
    // =========================
    @Column(name = "address", nullable = false)
    private String address;

    // =========================
    // PDF RUT (en base64)
    // =========================
    @Column(name = "rut_pdf_base64", columnDefinition = "TEXT")
    private String rutPdfBase64;

    @Column(name = "rut_pdf_nombre", length = 255)
    private String rutPdfNombre;

    // =========================
    // FECHA CREACIÓN
    // =========================
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}