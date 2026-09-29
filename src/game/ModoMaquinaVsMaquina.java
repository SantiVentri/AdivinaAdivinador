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
import utils.Registro;


public class ModoMaquinaVsMaquina {
	private final Random random = new Random();
	private final Scanner scanner;

	public ModoMaquinaVsMaquina(Scanner scanner) {
		this.scanner = scanner;
	}

	public void jugar() {
		List<Personaje> personajes = PersonajeFactory.crearPersonajes();
		HistorialConsultas historial = new HistorialConsultas();
        Registro registro = new Registro();

		MaquinaAsertiva asertiva = new MaquinaAsertiva(new Tablero(personajes), historial, registro);
		MaquinaAleatoria aleatoria = new MaquinaAleatoria(new Tablero(personajes), historial, registro);

		asertiva.elegirPersonaje(azar(personajes));
		aleatoria.elegirPersonaje(azar(personajes));

        registro.registrar("\n########## MÁQUINA vs MÁQUINA (sos espectador) ##########");

		Jugador ganador = new MotorJuego(asertiva, aleatoria, historial,
				jugadorQueJugo -> Consola.esperarEnter(scanner), registro).jugar();

		if (ganador == null) {
            registro.registrar("\nLa partida terminó en empate.");
		} else {
            registro.registrar("\nGanó: " + ganador.getNombre());
		}
	}

	private Personaje azar(List<Personaje> personajes) {
		return personajes.get(random.nextInt(personajes.size()));
	}
}
