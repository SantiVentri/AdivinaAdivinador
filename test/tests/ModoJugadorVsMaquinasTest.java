package tests;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import game.ModoJugadorVsMaquinas;
import model.Personaje;
import utils.PersonajeFactory;

/**
 * Partidas completas de Jugador vs Máquinas conducidas con un guion que se repite para
 * siempre (nunca hay EOF), así el humano simulado siempre tiene una respuesta válida sin
 * importar cuántos turnos dure la partida. Cada corrida va en un hilo con watchdog.
 */
public class ModoJugadorVsMaquinasTest {

	static final int TIMEOUT_MS = 15000;

	public static void run() {
		List<Personaje> personajes = PersonajeFactory.crearPersonajes();
		int nombres = personajes.size();

		// Guion 1: el humano SIEMPRE pregunta (opción 2), tipo 1, valor 1. Nunca arriesga.
		// Termina cuando alguna máquina adivina el secreto del humano -> rama "perdiste".
		String soloPreguntar = "1\n" + repetir("2\n1\n1\n", 40);

		// Guion 2: el humano prueba arriesgar los 23 nombres en orden, uno por turno.
		// En <=23 turnos acierta y gana la ronda -> ejercita replicarPreguntasPrevias, ronda 2 y score.
		StringBuilder arriesgarTodos = new StringBuilder("1\n"); // elige personaje secreto = #1
		for (int i = 0; i < nombres; i++) {
			arriesgarTodos.append("1\n").append(personajes.get(i).getNombre()).append("\n");
		}

		// Guion 3: mezcla: a veces pregunta, a veces arriesga un nombre.
		StringBuilder mixto = new StringBuilder("5\n");
		for (int i = 0; i < nombres; i++) {
			mixto.append("2\n1\n1\n"); // preguntar
			mixto.append("1\n").append(personajes.get(i).getNombre()).append("\n"); // arriesgar
		}

		correr("solo-preguntar (pierde la ronda 1)", soloPreguntar, 3);
		int r2a = correr("arriesgar-todos (gana rondas, score, ronda 2)", arriesgarTodos.toString(), 14);
		int r2b = correr("mixto preguntar/arriesgar", mixto.toString(), 6);

		T.check(r2a + r2b >= 1,
				"al menos una partida completa llegó a la ronda 2 sin romperse: replicarPreguntasPrevias + "
						+ "setup de ronda 2 ejercitados (" + (r2a + r2b) + " veces)");

		T.section("ModoJugadorVsMaquinas - doble registro de victoria");
		T.finding("SCORE-DOUBLE-1", "BAJA",
				"ModoJugadorVsMaquinas llama scoreRepository.registrarVictoria(nombre) DOS veces cuando el jugador "
						+ "completa el desafío: una al ganarle a la Máquina Aleatoria y otra al ganarle a la Asertiva. "
						+ "Un desafío completo suma 2 al marcador. Si la idea es 'partidas ganadas', quizás debería sumar 1 "
						+ "por desafío (o contar cada ronda como partida, pero entonces la derrota en ronda 1 también "
						+ "debería contar como partida jugada). Es una ambigüedad de la consigna, no un crash.");
	}

	static int correr(String etiqueta, String guion, int veces) {
		T.section("ModoJugadorVsMaquinas - " + etiqueta + " (x" + veces + ")");

		int ok = 0;
		int ronda2 = 0;
		int ganoTodo = 0;
		int perdio = 0;

		for (int i = 0; i < veces; i++) {
			InputStream inReal = System.in;
			PrintStream outReal = System.out;
			ByteArrayOutputStream cap = new ByteArrayOutputStream();
			AtomicReference<Throwable> err = new AtomicReference<>();

			System.setIn(T.repeating(guion));
			System.setOut(new PrintStream(cap, true, StandardCharsets.UTF_8));
			Thread th = new Thread(() -> {
				try {
					new ModoJugadorVsMaquinas("Tester", new java.util.Scanner(System.in)).jugar();
				} catch (Throwable t) {
					err.set(t);
				}
			});
			th.setDaemon(true);
			th.start();
			try {
				th.join(TIMEOUT_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			System.setIn(inReal);
			System.setOut(outReal);

			String salida = cap.toString(StandardCharsets.UTF_8);

			if (th.isAlive()) {
				T.fail("(" + etiqueta + " #" + i + ") no terminó en " + TIMEOUT_MS + " ms (posible cuelgue)");
				continue;
			}
			if (err.get() != null) {
				T.fail("(" + etiqueta + " #" + i + ") lanzó " + err.get().getClass().getName() + ": " + err.get().getMessage());
				continue;
			}
			ok++;
			if (salida.contains("Máquina Asertiva")) {
				ronda2++;
			}
			if (salida.contains("Desafío completado")) {
				ganoTodo++;
			}
			if (salida.contains("El desafío termina acá") || salida.contains("Perdiste")) {
				perdio++;
			}
		}

		T.check(ok == veces, "(" + etiqueta + ") " + ok + "/" + veces + " partidas completas sin excepción ni cuelgue");
		T.realOut().println("    (alcanzaron ronda 2 [Asertiva]: " + ronda2 + "/" + veces
				+ "   ganaron el desafío completo: " + ganoTodo + "   perdieron ronda 1: " + perdio + ")");
		return ronda2;
	}

	static String repetir(String s, int veces) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < veces; i++) {
			sb.append(s);
		}
		return sb.toString();
	}
}
