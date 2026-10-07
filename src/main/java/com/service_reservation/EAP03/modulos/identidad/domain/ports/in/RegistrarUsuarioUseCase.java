package com.service_reservation.EAP03.modulos.identidad.domain.ports.in;
import com.service_reservation.EAP03.modulos.identidad.domain.model.UsuarioNuevoComando;

public interface RegistrarUsuarioUseCase {
    void ejecutar(UsuarioNuevoComando comando);
    void guardarPerfilCliente(Long idUsuario, String nombre, String telefono, String documento);
    void guardarPerfilProveedor(Long idUsuario, String razonSocial, String telefono, String nitRut);
}