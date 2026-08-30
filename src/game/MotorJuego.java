package game;

import java.util.HashMap;
import java.util.Map;

import model.FiltroAplicado;
import model.Personaje;
import players.Jugador;

public class MotorJuego {
	private static final int INTENTOS_FALLIDOS_PARA_PERDER = 3;

	private final Jugador jugador1;
	private final Jugador jugador2;
	private final Map<Jugador, Integer> intentosFallidos;

	private Jugador ganador;
	private boolean partidaTerminada;

	public MotorJuego(Jugador jugador1, Jugador jugador2) {
		if (jugador1 == null || jugador2 == null) {
			throw new IllegalArgumentException("Los dos jugadores son obligatorios.");
		}
		if (!jugador1.tienePersonajeElegido() || !jugador2.tienePersonajeElegido()) {
			throw new IllegalStateException("Ambos jugadores deben tener un personaje secreto elegido antes de iniciar la partida.");
		}

		this.jugador1 = jugador1;
		this.jugador2 = jugador2;
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

			jugarTurno(activo, pasivo, numeroTurno);

			if (partidaTerminada) {
				break;
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
			resolverIntento(activo, pasivo, intento);
			return;
		}

		FiltroAplicado filtro = activo.hacerPregunta();
		if (filtro == null) {
			System.out.println(activo.getNombre() + " no tiene más preguntas nuevas para hacer, pasa el turno.");
			return;
		}

		boolean respuesta = pasivo.responderPregunta(filtro);
		System.out.println(activo.getNombre() + " pregunta -> " + filtro.getTipo() + " = " + filtro.getValor());
		System.out.println(pasivo.getNombre() + " responde -> " + (respuesta ? "Sí" : "No"));

		activo.filtrarOpciones(filtro, respuesta);
		System.out.println(activo.getNombre() + " tiene ahora " + activo.getTablero().cantidadRestante() + " personaje(s) posible(s).");
	}

	private void resolverIntento(Jugador activo, Jugador pasivo, Personaje intento) {
		System.out.println(activo.getNombre() + " arriesga el personaje: " + intento.getNombre());

		if (pasivo.esPersonajeSecreto(intento)) {
			System.out.println("¡Correcto! Era el personaje secreto de " + pasivo.getNombre() + ".");
			declararGanador(activo);
			return;
		}

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
