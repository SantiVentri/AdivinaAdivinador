package model;

public class Filtro {
    private final TipoFiltro tipo;
    private final String nombre;
    private final String[] valores;

    public Filtro(TipoFiltro tipo, String nombre, String[] valores) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.valores = valores;
    }

    public TipoFiltro getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public String[] getValores() {
        return valores;
    }
}
