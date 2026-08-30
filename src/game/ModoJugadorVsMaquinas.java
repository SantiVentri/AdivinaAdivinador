package game;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Consumer;

import model.FiltroAplicado;
import model.Personaje;
import model.Tablero;
import players.Consulta;
import players.HistorialConsultas;
import players.JugadorHumano;
import players.Jugador;
import players.MaquinaAleatoria;
import players.MaquinaAsertiva;
import utils.Consola;
import utils.PersonajeFactory;

/**
 * Modo secuencial: el jugador juega primero contra la Máquina Aleatoria.
 * Si gana, entra la Máquina Asertiva a jugar contra él conociendo todas las
 * preguntas que hizo la Aleatoria. El jugador conserva su personaje secreto;
 * cada máquina usa el suyo.
 */
public class ModoJugadorVsMaquinas {
	private final String nombre;
	private final Scanner scanner;
	private final Random random = new Random();

	public ModoJugadorVsMaquinas(String nombre, Scanner scanner) {
		this.nombre = nombre;
		this.scanner = scanner;
	}

	public void jugar() {
		System.out.println("\n########## JUGADOR vs MÁQUINAS ##########");

		// Una sola lista de personajes para toda la sesión: todos los tableros comparten
		// las mismas instancias (los intentos se comparan por identidad) y así el
		// personaje secreto del jugador sigue estando en el tablero de la Asertiva.
		List<Personaje> personajes = PersonajeFactory.crearPersonajes();
		HistorialConsultas historial = new HistorialConsultas();

		// Después del turno de una máquina se espera un Enter para dar tiempo a leer el log.
		// No se pausa después del turno del jugador (recién terminó de interactuar).
		Consumer<Jugador> pausaTrasMaquina = jugadorQueJugo -> {
			if (!(jugadorQueJugo instanceof JugadorHumano)) {
				Consola.esperarEnter(scanner);
			}
		};

		Personaje secretoJugador = elegirPersonajeSecreto(personajes);

		// ---------- Ronda 1: Jugador vs Máquina Aleatoria ----------
		JugadorHumano jugador1 = new JugadorHumano(nombre, new Tablero(personajes), scanner);
		jugador1.elegirPersonaje(secretoJugador);

		MaquinaAleatoria aleatoria = new MaquinaAleatoria(new Tablero(personajes), historial);
		aleatoria.elegirPersonaje(azar(personajes));

		Jugador ganador1 = new MotorJuego(jugador1, aleatoria, historial, pausaTrasMaquina).jugar();

		if (ganador1 != jugador1) {
			System.out.println("\nPerdiste contra la Máquina Aleatoria. El desafío termina acá.");
			return;
		}

		System.out.println("\n¡Ganaste la primera ronda! Ahora entra la Máquina Asertiva.");

		// ---------- Ronda 2: Jugador (mismo personaje) vs Máquina Asertiva ----------
		// Se usa un JugadorHumano nuevo porque elegirPersonaje es de un solo uso y el
		// Tablero no se puede reasignar; lo que se conserva es el Personaje secreto.
		Tablero tableroAsertiva = new Tablero(personajes);
		JugadorHumano jugador2 = new JugadorHumano(nombre, new Tablero(personajes), scanner);
		jugador2.elegirPersonaje(secretoJugador);

		MaquinaAsertiva asertiva = new MaquinaAsertiva(tableroAsertiva, historial);
		asertiva.elegirPersonaje(azar(personajes));

		int heredadas = replicarPreguntasPrevias(historial, aleatoria.getNombre(), asertiva.getNombre(), tableroAsertiva);
		System.out.println("\nLa Máquina Asertiva entra conociendo " + heredadas + " pregunta(s) previa(s); "
				+ "arranca con " + tableroAsertiva.cantidadRestante() + " personaje(s) posible(s).");

		Jugador ganador2 = new MotorJuego(jugador2, asertiva, historial, pausaTrasMaquina).jugar();

		if (ganador2 == jugador2) {
			System.out.println("\n¡Le ganaste también a la Máquina Asertiva! Desafío completado.");
		} else if (ganador2 == null) {
			System.out.println("\nLa segunda ronda terminó en empate.");
		} else {
			System.out.println("\nLa Máquina Asertiva te ganó la segunda ronda.");
		}
	}

	// Aplica sobre el tablero de la Asertiva todas las preguntas que hizo la Aleatoria
	// (fueron sobre el personaje del jugador, que no cambió) y las registra a nombre de
	// la Asertiva para que no las vuelva a preguntar. Devuelve cuántas replicó.
	private int replicarPreguntasPrevias(HistorialConsultas historial, String nombreAleatoria,
			String nombreAsertiva, Tablero tableroAsertiva) {
		List<Consulta> previas = historial.obtenerConsultasDe(nombreAleatoria);
		for (Consulta c : previas) {
			FiltroAplicado f = c.getFiltro();
			tableroAsertiva.aplicarFiltro(f.getTipo(), f.getValor(), c.getRespuesta());
			historial.agregarConsulta(nombreAsertiva, f, c.getRespuesta());
		}
		return previas.size();
	}

	private Personaje elegirPersonajeSecreto(List<Personaje> personajes) {
		System.out.println("\nElegí tu personaje secreto (el que las máquinas tienen que adivinar):");
		for (int i = 0; i < personajes.size(); i++) {
			System.out.println((i + 1) + ". " + personajes.get(i));
		}

		while (true) {
			System.out.print("\n>> ");
			try {
				int opcion = Integer.parseInt(scanner.nextLine().trim());
				if (opcion >= 1 && opcion <= personajes.size()) {
					Personaje elegido = personajes.get(opcion - 1);
					System.out.println("Tu personaje secreto es: " + elegido.getNombre());
					return elegido;
				}
			} catch (NumberFormatException e) {
				// se vuelve a pedir
			}
			System.out.println("Opción inválida, probá de nuevo.");
		}
	}

	private Personaje azar(List<Personaje> personajes) {
		return personajes.get(random.nextInt(personajes.size()));
	}
}
