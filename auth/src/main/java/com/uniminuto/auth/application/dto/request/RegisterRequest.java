package com.uniminuto.auth.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "Formato de correo electrónico inválido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 50, message = "La contraseña debe tener entre 8 y 50 caracteres")
    private String password;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    @Size(max = 50, message = "El apellido no puede exceder 50 caracteres")
    private String lastName;
}
