package com.uniminuto.auth.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClienteResponse {

    private Long id;

    private String documentNumber;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String address;

    private String rutPdfNombre;
}