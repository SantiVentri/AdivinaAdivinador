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

    // Clave canónica de la pregunta, usada por el historial y las máquinas: "TIPO=valor"
    public String clave() {
        return clave(tipo, valor);
    }

    public static String clave(TipoFiltro tipo, String valor) {
        return tipo.name() + "=" + valor;
    }

    @Override
    public String toString() {
        return clave();
    }
}