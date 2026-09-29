package game;

import java.util.function.Consumer;
import model.FiltroAplicado;
import model.Personaje;
import players.HistorialConsultas;
import players.Jugador;
import utils.Registro;

public class MotorJuego {

	private final Jugador jugador1;
	private final Jugador jugador2;
	private final HistorialConsultas historial;
	private final Consumer<Jugador> alTerminarTurno;
    private final Registro registro;

	private Jugador ganador;
	private boolean partidaTerminada;

	public MotorJuego(Jugador jugador1, Jugador jugador2, HistorialConsultas historial,
                      Consumer<Jugador> alTerminarTurno, Registro registro) {
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
		this.alTerminarTurno = alTerminarTurno;
        this.registro = registro;
	}

	public Jugador jugar() {
        registro.registrar("\n==================================================" + "\n"
		+ "Comienza la partida: " + jugador1.getNombre() + " vs. " + jugador2.getNombre() + "\n"
		+ "==================================================");

		Jugador activo = jugador1;
		Jugador pasivo = jugador2;
		int numeroTurno = 1;

		while (!partidaTerminada) {
			if (activo.getTablero().estaVacio() && pasivo.getTablero().estaVacio()) {
                registro.registrar("\nNo quedan personajes posibles para ninguno de los dos. ¡Empate!");
				break;
			}

			jugarTurno(activo, pasivo, numeroTurno);

			if (partidaTerminada) {
				break;
			}

			if (alTerminarTurno != null) {
				alTerminarTurno.accept(activo);
			}

			Jugador siguienteActivo = pasivo;
			Jugador siguientePasivo = activo;
			activo = siguienteActivo;
			pasivo = siguientePasivo;
			numeroTurno++;
		}

		return ganador;
	}

	private void jugarTurno(Jugador activo, Jugador pasivo, int numeroTurno) {
        registro.registrar("\n--- Turno " + numeroTurno + ": le toca a " + activo.getNombre() + " ---");

		Personaje intento = activo.arriesgarPersonaje();

		if (intento != null) {
			resolverIntento(activo, pasivo, intento);
			return;
		}

		FiltroAplicado filtro = activo.hacerPregunta();
		if (filtro == null) {
            registro.registrar(activo.getNombre()
                    + " no tiene más preguntas nuevas para hacer, pasa el turno.");
			return;
		}

		boolean respuesta = pasivo.responderPregunta(filtro);
		historial.agregarConsulta(activo.getNombre(), filtro, respuesta);
		registro.registrar(activo.getNombre() + " pregunta -> " + filtro.getTipo() + " = "
                + filtro.getValor() + "?");
		registro.registrar(pasivo.getNombre() + " responde -> " + (respuesta ? "Sí." : "No."));

		activo.filtrarOpciones(filtro, respuesta);
		int restantesDespues = activo.getTablero().cantidadRestante();
        registro.registrar(activo.getNombre() + " tiene ahora " + restantesDespues
                + " personaje(s) posible(s).");
	}

	private void resolverIntento(Jugador activo, Jugador pasivo, Personaje intento) {
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

	private void declararGanador(Jugador jugador) {
		this.ganador = jugador;
		this.partidaTerminada = true;
        registro.registrar(
                "\n**************************************************" + "\n"
                + jugador.getNombre() + " gana la partida." + "\n"
                + "**************************************************"
        );
	}

	public Jugador getGanador() {
		return ganador;
	}

	public boolean isPartidaTerminada() {
		return partidaTerminada;
	}
}
