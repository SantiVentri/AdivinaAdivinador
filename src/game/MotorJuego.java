package game;

import model.FiltroAplicado;
import model.Personaje;
import players.HistorialConsultas;
import players.Jugador;
import players.JugadorHumano;
import players.JugadorMaquina;
import utils.Registro;

public class MotorJuego {

    private final Jugador jugador1;
    private final Jugador jugador2;
    private final HistorialConsultas historial;
    private final Registro registro;

    private Jugador activo;
    private Jugador pasivo;
    private int numeroTurno;

    private Jugador ganador;
    private boolean partidaTerminada;

    public MotorJuego(Jugador jugador1, Jugador jugador2, HistorialConsultas historial, Registro registro) {
        if (jugador1 == null || jugador2 == null) {
            throw new IllegalArgumentException("Los dos jugadores son obligatorios.");
        }
        if (historial == null) {
            throw new IllegalArgumentException("El historial de consultas es obligatorio.");
        }
        if (registro == null) {
            throw new IllegalArgumentException("El registro de eventos es obligatorio.");
        }
        if (!jugador1.tienePersonajeElegido() || !jugador2.tienePersonajeElegido()) {
            throw new IllegalStateException("Ambos jugadores deben tener un personaje secreto elegido antes de iniciar la partida.");
        }

        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.historial = historial;
        this.registro = registro;

        this.activo = jugador1;
        this.pasivo = jugador2;
        this.numeroTurno = 1;
    }

    public void iniciar() {
        registro.registrar("\n==================================================" + "\n"
                + "Comienza la partida: " + jugador1.getNombre() + " vs. " + jugador2.getNombre() + "\n"
                + "==================================================");
        anunciarTurno();
    }

    // ---------- Acciones del humano (las llama la pantalla) ----------

    public void preguntar(FiltroAplicado filtro) {
        validarTurnoHumano();
        resolverPregunta(filtro);
        terminarTurno();
    }

    public void arriesgar(Personaje personaje) {
        validarTurnoHumano();
        resolverIntento(personaje);
        terminarTurno();
    }

    // ---------- Turno de una máquina ----------

    public void jugarTurnoMaquina() {
        if (partidaTerminada) {
            throw new IllegalStateException("La partida ya terminó.");
        }
        if (!(activo instanceof JugadorMaquina)) {
            throw new IllegalStateException("Le toca jugar a " + activo.getNombre() + ", no a una máquina.");
        }

        JugadorMaquina maquina = (JugadorMaquina) activo;
        Personaje intento = maquina.arriesgarPersonaje();

        if (intento != null) {
            resolverIntento(intento);
        } else {
            FiltroAplicado filtro = maquina.hacerPregunta();
            if (filtro == null) {
                registro.registrar(activo.getNombre() + " no tiene más preguntas nuevas para hacer, pasa el turno.");
            } else {
                resolverPregunta(filtro);
            }
        }

        terminarTurno();
    }

    // ---------- Lógica de cada jugada ----------

    private void resolverPregunta(FiltroAplicado filtro) {
        boolean respuesta = pasivo.responderPregunta(filtro);
        historial.agregarConsulta(activo.getNombre(), filtro, respuesta);
        registro.registrar(activo.getNombre() + " pregunta -> " + filtro.getTipo() + " = " + filtro.getValor() + "?");
        registro.registrar(pasivo.getNombre() + " responde -> " + (respuesta ? "Sí." : "No."));

        activo.filtrarOpciones(filtro, respuesta);
        registro.registrar(activo.getNombre() + " tiene ahora " + activo.getTablero().cantidadRestante()
                + " personaje(s) posible(s).");
    }

    private void resolverIntento(Personaje intento) {
        registro.registrar(activo.getNombre() + " arriesga el personaje: " + intento.getNombre());

        if (pasivo.esPersonajeSecreto(intento)) {
            registro.registrar("¡Correcto! Era el personaje secreto de " + pasivo.getNombre() + ".");
            declararGanador(activo);
            return;
        }

        // El intento fue incorrecto: ese personaje ya no es una opción posible, se saca del tablero.
        activo.getTablero().sacarPersonaje(intento);
        registro.registrar("Incorrecto. " + activo.getNombre() + " El juego continua!!.");
    }

    // Pasa el turno al otro jugador, salvo que la partida haya terminado.
    private void terminarTurno() {
        if (partidaTerminada) {
            return;
        }

        Jugador anterior = activo;
        activo = pasivo;
        pasivo = anterior;
        numeroTurno++;

        if (activo.getTablero().estaVacio() && pasivo.getTablero().estaVacio()) {
            registro.registrar("\nNo quedan personajes posibles para ninguno de los dos. ¡Empate!");
            partidaTerminada = true;
            return;
        }

        anunciarTurno();
    }

    private void anunciarTurno() {
        registro.registrar("\n--- Turno " + numeroTurno + ": le toca a " + activo.getNombre() + " ---");
    }

    private void validarTurnoHumano() {
        if (partidaTerminada) {
            throw new IllegalStateException("La partida ya terminó.");
        }
        if (!(activo instanceof JugadorHumano)) {
            throw new IllegalStateException("No es el turno del jugador humano.");
        }
    }

    private void declararGanador(Jugador jugador) {
        this.ganador = jugador;
        this.partidaTerminada = true;
        registro.registrar("\n**************************************************" + "\n"
                + jugador.getNombre() + " gana la partida." + "\n"
                + "**************************************************");
    }

    // ---------- Consultas para la pantalla ----------

    public boolean esTurnoDelHumano() {
        return !partidaTerminada && activo instanceof JugadorHumano;
    }

    public Jugador getJugadorActivo() {
        return activo;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public boolean isPartidaTerminada() {
        return partidaTerminada;
    }
}