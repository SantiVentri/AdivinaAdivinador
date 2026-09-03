package game;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

import model.Personaje;
import model.Tablero;
import players.HistorialConsultas;
import players.Jugador;
import players.MaquinaAleatoria;
import players.MaquinaAsertiva;
import utils.Consola;
import utils.PersonajeFactory;


public class ModoMaquinaVsMaquina {
	private final Random random = new Random();
	private final Scanner scanner;

	public ModoMaquinaVsMaquina(Scanner scanner) {
		this.scanner = scanner;
	}

	public void jugar() {
		System.out.println("\n########## MÁQUINA vs MÁQUINA (sos espectador) ##########");

		
		List<Personaje> personajes = PersonajeFactory.crearPersonajes();
		HistorialConsultas historial = new HistorialConsultas();

		MaquinaAsertiva asertiva = new MaquinaAsertiva(new Tablero(personajes), historial);
		MaquinaAleatoria aleatoria = new MaquinaAleatoria(new Tablero(personajes), historial);

		asertiva.elegirPersonaje(azar(personajes));
		aleatoria.elegirPersonaje(azar(personajes));

		Jugador ganador = new MotorJuego(asertiva, aleatoria, historial,
				jugadorQueJugo -> Consola.esperarEnter(scanner)).jugar();

		if (ganador == null) {
			System.out.println("\nLa partida terminó en empate.");
		} else {
			System.out.println("\nGanó: " + ganador.getNombre());
		}
	}

	private Personaje azar(List<Personaje> personajes) {
		return personajes.get(random.nextInt(personajes.size()));
	}
}
