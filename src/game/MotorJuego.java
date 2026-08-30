package game;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import model.FiltroAplicado;
import model.Personaje;
import players.HistorialConsultas;
import players.Jugador;

public class MotorJuego {
	private static final int INTENTOS_FALLIDOS_PARA_PERDER = 3;
	private static final int MAX_TURNOS_SIN_PROGRESO = 4;

	private final Jugador jugador1;
	private final Jugador jugador2;
	private final Map<Jugador, Integer> intentosFallidos;
	private final HistorialConsultas historial;
	private final Consumer<Jugador> alTerminarTurno;

	private Jugador ganador;
	private boolean partidaTerminada;
	private int turnosSinProgreso;

	public MotorJuego(Jugador jugador1, Jugador jugador2) {
		this(jugador1, jugador2, new HistorialConsultas());
	}

	public MotorJuego(Jugador jugador1, Jugador jugador2, HistorialConsultas historial) {
		this(jugador1, jugador2, historial, null);
	}

	/**
	 * @param alTerminarTurno callback opcional invocado con el jugador que acaba de jugar,
	 *                        cada vez que termina un turno y la partida sigue en juego
	 *                        (los modos lo usan para pausar y dejar leer el log).
	 */
	public MotorJuego(Jugador jugador1, Jugador jugador2, HistorialConsultas historial, Consumer<Jugador> alTerminarTurno) {
		if (jugador1 == null || jugador2 == null) {
			throw new IllegalArgumentException("Los dos jugadores son obligatorios.");
		}
		if (historial == null) {
			throw new IllegalArgumentException("El historial de consultas es obligatorio.");
		}
		if (!jugador1.tienePersonajeElegido() || !jugador2.tienePersonajeElegido()) {
			throw new IllegalStateException("Ambos jugadores deben tener un personaje secreto elegido antes de iniciar la partida.");
		}

		this.jugador1 = jugador1;
		this.jugador2 = jugador2;
		this.historial = historial;
		this.alTerminarTurno = alTerminarTurno;
		this.intentosFallidos = new HashMap<>();
		this.intentosFallidos.put(jugador1, 0);
		this.intentosFallidos.put(jugador2, 0);
	}

	public Jugador jugar() {
		System.out.println("\n==================================================");
		System.out.println("Comienza la partida: " + jugador1.getNombre() + " vs. " + jugador2.getNombre());
		System.out.println("==================================================");

		Jugador activo = jugador1;
		Jugador pasivo = jugador2;
		int numeroTurno = 1;

		while (!partidaTerminada) {
			if (activo.getTablero().estaVacio() && pasivo.getTablero().estaVacio()) {
				System.out.println("\nNo quedan personajes posibles para ninguno de los dos. ¡Empate!");
				break;
			}

			if (turnosSinProgreso >= MAX_TURNOS_SIN_PROGRESO) {
				System.out.println("\nNadie avanza hace varios turnos. ¡Empate!");
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
		System.out.println("\n--- Turno " + numeroTurno + ": le toca a " + activo.getNombre() + " ---");

		Personaje intento = activo.arriesgarPersonaje();

		if (intento != null) {
			turnosSinProgreso = 0;
			resolverIntento(activo, pasivo, intento);
			return;
		}

		FiltroAplicado filtro = activo.hacerPregunta();
		if (filtro == null) {
			System.out.println(activo.getNombre() + " no tiene más preguntas nuevas para hacer, pasa el turno.");
			turnosSinProgreso++;
			return;
		}

		int restantesAntes = activo.getTablero().cantidadRestante();
		boolean respuesta = pasivo.responderPregunta(filtro);
		historial.agregarConsulta(activo.getNombre(), filtro, respuesta);
		System.out.println(activo.getNombre() + " pregunta -> " + filtro.getTipo() + " = " + filtro.getValor());
		System.out.println(pasivo.getNombre() + " responde -> " + (respuesta ? "Sí" : "No"));

		activo.filtrarOpciones(filtro, respuesta);
		int restantesDespues = activo.getTablero().cantidadRestante();
		System.out.println(activo.getNombre() + " tiene ahora " + restantesDespues + " personaje(s) posible(s).");

		turnosSinProgreso = (restantesDespues < restantesAntes) ? 0 : turnosSinProgreso + 1;
	}

	private void resolverIntento(Jugador activo, Jugador pasivo, Personaje intento) {
		System.out.println(activo.getNombre() + " arriesga el personaje: " + intento.getNombre());

		if (pasivo.esPersonajeSecreto(intento)) {
			System.out.println("¡Correcto! Era el personaje secreto de " + pasivo.getNombre() + ".");
			declararGanador(activo);
			return;
		}

		// El intento fue incorrecto: ese personaje ya no es una opción posible, se saca del tablero.
		activo.getTablero().sacarPersonaje(intento);

		int fallos = intentosFallidos.get(activo) + 1;
		intentosFallidos.put(activo, fallos);

		if (fallos >= INTENTOS_FALLIDOS_PARA_PERDER) {
			System.out.println("Incorrecto. Era el " + fallos + "° intento fallido de " + activo.getNombre() + ", ¡pierde la partida!");
			declararGanador(pasivo);
		} else {
			int oportunidadesRestantes = INTENTOS_FALLIDOS_PARA_PERDER - fallos;
			System.out.println("Incorrecto. " + activo.getNombre() + " lleva " + fallos + " intento(s) fallido(s). "
					+ "Le queda(n) " + oportunidadesRestantes + " oportunidad(es) más antes de perder.");
		}
	}

	private void declararGanador(Jugador jugador) {
		this.ganador = jugador;
		this.partidaTerminada = true;
		System.out.println("\n**************************************************");
		System.out.println(jugador.getNombre() + " gana la partida.");
		System.out.println("**************************************************");
	}

	public Jugador getGanador() {
		return ganador;
	}

	public boolean isPartidaTerminada() {
		return partidaTerminada;
	}
}
