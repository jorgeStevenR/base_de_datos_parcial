package com.uniminuto.auth.application.dto.request;

import lombok.Data;

@Data
public class ClienteRequest {

    private String documentNumber;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String address;
}