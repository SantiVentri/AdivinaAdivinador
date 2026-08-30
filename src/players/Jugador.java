package players;

import model.FiltroAplicado;
import model.Personaje;
import model.Tablero;

public abstract class Jugador {
    private final String nombre;
    private Personaje personajeSecreto;
    private final Tablero tablero;

    protected Jugador(String nombre, Tablero tablero) {
        this.nombre = nombre;
        this.tablero = tablero;
    }

    public final void elegirPersonaje(Personaje personaje) {
        if (this.personajeSecreto != null) {
            throw new IllegalStateException("El personaje secreto ya fue elegido y no puede modificarse.");
        }
        this.personajeSecreto = personaje;
    }

    public abstract FiltroAplicado hacerPregunta();

    public abstract Personaje arriesgarPersonaje();

    public void filtrarOpciones(FiltroAplicado filtro, boolean respuestaEsperada) {
    	tablero.aplicarFiltro(filtro.getTipo(), filtro.getValor(), respuestaEsperada);
    }
    
    public boolean responderPregunta(FiltroAplicado filtro) {
        if (this.personajeSecreto == null) {
            throw new IllegalStateException("El jugador aún no tiene asignado un personaje secreto.");
        }
        return this.personajeSecreto.cumpleFiltro(filtro.getTipo(), filtro.getValor());
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public Tablero getTablero() {
        return tablero;
    }

    protected Personaje getPersonajeSecreto() {
        return personajeSecreto;
    }
}