package utils;

import java.util.ArrayList;
import java.util.List;

public class Registro {
    private final List<String> mensajesPendientes = new ArrayList<>();

    public void registrar(String mensaje) {
        mensajesPendientes.add(mensaje);
    }

    public List<String> retirarMensajes() {
        List<String> mensajes = new ArrayList<>(mensajesPendientes);
        mensajesPendientes.clear();
        return mensajes;
    }
}
