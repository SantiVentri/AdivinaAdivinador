package model;

public class FiltroAplicado {
    private final TipoFiltro tipo;
    private final String valor;

    // Constructor
    public FiltroAplicado(TipoFiltro tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    // Getter
    public TipoFiltro getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

}