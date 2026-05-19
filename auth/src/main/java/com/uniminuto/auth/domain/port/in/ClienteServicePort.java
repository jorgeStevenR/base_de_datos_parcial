package com.uniminuto.auth.domain.port.in;

import com.uniminuto.auth.application.dto.request.ClienteRequest;
import com.uniminuto.auth.domain.model.Cliente;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ClienteServicePort {

    Cliente crearCliente(ClienteRequest request, MultipartFile archivo) throws IOException;

    List<Cliente> listarClientes();

    Cliente buscarPorCedula(String documentNumber);

    Cliente buscarPorId(Long id);

    File generarClientePdf(Long id) throws IOException;

    void eliminarCliente(Long id);
}
