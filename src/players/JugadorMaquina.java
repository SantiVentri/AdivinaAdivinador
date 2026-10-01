package players;

import model.FiltroAplicado;
import model.Personaje;
import model.Tablero;

// Un jugador que decide solo qué hacer en su turno. Cada máquina define su propia estrategia.
public abstract class JugadorMaquina extends Jugador {

    protected JugadorMaquina(String nombre, Tablero tablero) {
        super(nombre, tablero);
    }

    // Devuelve la pregunta que quiere hacer, o null si ya no le quedan preguntas nuevas.
    public abstract FiltroAplicado hacerPregunta();

    // Devuelve el personaje que arriesga, o null si este turno prefiere preguntar.
    public abstract Personaje arriesgarPersonaje();
}