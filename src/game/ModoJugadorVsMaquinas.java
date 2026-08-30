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
import score.ScoreRepository;
import utils.Consola;
import utils.PersonajeFactory;


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

		
		List<Personaje> personajes = PersonajeFactory.crearPersonajes();
		HistorialConsultas historial = new HistorialConsultas();
		ScoreRepository scoreRepository = new ScoreRepository();

		
		Consumer<Jugador> pausaTrasMaquina = jugadorQueJugo -> {
			if (!(jugadorQueJugo instanceof JugadorHumano)) {
				Consola.esperarEnter(scanner);
			}
		};

		Personaje secretoJugador = elegirPersonajeSecreto(personajes);

		
		JugadorHumano jugador1 = new JugadorHumano(nombre, new Tablero(personajes), scanner);
		jugador1.elegirPersonaje(secretoJugador);

		MaquinaAleatoria aleatoria = new MaquinaAleatoria(new Tablero(personajes), historial);
		aleatoria.elegirPersonaje(azar(personajes));

		Jugador ganador1 = new MotorJuego(jugador1, aleatoria, historial, pausaTrasMaquina).jugar();

		if (ganador1 != jugador1) {
			System.out.println("\nPerdiste contra la Máquina Aleatoria. El desafío termina acá.");
			return;
		}

		scoreRepository.registrarVictoria(nombre);
		System.out.println("\n¡Ganaste la primera ronda! Ahora entra la Máquina Asertiva.");

		
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
			scoreRepository.registrarVictoria(nombre);
			System.out.println("\n¡Le ganaste también a la Máquina Asertiva! Desafío completado.");
		} else if (ganador2 == null) {
			System.out.println("\nLa segunda ronda terminó en empate.");
		} else {
			System.out.println("\nLa Máquina Asertiva te ganó la segunda ronda.");
		}
	}

	
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
