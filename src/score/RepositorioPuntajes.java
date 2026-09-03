package score;

import java.util.List;
import java.util.Map;

public interface RepositorioPuntajes {

    /** Suma una victoria al jugador y persiste el cambio. */
    void registrarVictoria(String nombreJugador);

    /** Cantidad de victorias registradas para el jugador (0 si no tiene). */
    int getVictorias(String nombreJugador);

    /** Marcador ordenado de mayor a menor cantidad de victorias. */
    List<Map.Entry<String, Integer>> obtenerPuntajesOrdenados();

    /** Imprime el marcador por consola. */
    void mostrar();
}
