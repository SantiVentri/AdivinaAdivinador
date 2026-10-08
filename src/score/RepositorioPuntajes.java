package score;

import java.util.List;

public interface RepositorioPuntajes {
    /** Suma una partida jugada al jugador y persiste el cambio. */
    void registrarPartidaJugada(String nombreJugador);

    /** Suma una ronda ganada al jugador y persiste el cambio. */
    void registrarRondaGanada(String nombreJugador);

    /** Suma una partida ganada (ambas rondas del desafío) al jugador y persiste el cambio. */
    void registrarPartidaGanada(String nombreJugador);

    /** Marcador ordenado por partidas ganadas y, a igualdad, por rondas ganadas (de mayor a menor). */
    List<Puntaje> obtenerPuntajesOrdenados();
}
