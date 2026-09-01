package tests;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

import game.ModoJugadorVsMaquinas;
import game.ModoMaquinaVsMaquina;

/**
 * Robustez de la capa de entrada: qué pasa cuando el stdin se agota o se cierra.
 */
public class IoRobustezTest {

	public static void run() {
		T.section("IO - ModoMaquinaVsMaquina con stdin vacío");
		{
			Throwable t = correrConTimeout(() -> {
				Scanner sc = new Scanner(T.stdin("")); // sin ni un Enter
				new ModoMaquinaVsMaquina(sc).jugar();
			}, 4000);

			if (t instanceof NoSuchElementException) {
				T.finding("IO-EOF-1", "MEDIA",
						"ModoMaquinaVsMaquina revienta con NoSuchElementException apenas se agota el stdin: el hook "
								+ "'Presioná Enter para continuar' (Consola.esperarEnter) hace scanner.nextLine() sin "
								+ "controlar el fin de entrada. Cualquier ejecución no interactiva (pipe, archivo redirigido, "
								+ "terminal cerrada) corta con stack trace en vez de terminar prolijo.");
			} else if (t == null) {
				T.fail("se esperaba NoSuchElementException con stdin vacío, no pasó nada");
			} else {
				T.fail("con stdin vacío se obtuvo " + t.getClass().getName() + " (se esperaba NoSuchElementException)");
			}
		}

		T.section("IO - ModoJugadorVsMaquinas con stdin vacío (elección de personaje)");
		{
			Throwable t = correrConTimeout(() -> {
				Scanner sc = new Scanner(T.stdin(""));
				new ModoJugadorVsMaquinas("Tester", sc).jugar();
			}, 4000);

			if (t instanceof NoSuchElementException) {
				T.finding("IO-EOF-2", "MEDIA",
						"ModoJugadorVsMaquinas.elegirPersonajeSecreto hace scanner.nextLine() y solo atrapa "
								+ "NumberFormatException: con stdin agotado sale NoSuchElementException sin controlar.");
			} else if (t == null) {
				T.fail("se esperaba NoSuchElementException, no pasó nada");
			} else {
				T.fail("se obtuvo " + t.getClass().getName() + " (se esperaba NoSuchElementException)");
			}
		}

		T.section("IO - ModoJugadorVsMaquinas: elige personaje y después se corta el input");
		{
			Throwable t = correrConTimeout(() -> {
				Scanner sc = new Scanner(T.stdin("1\n")); // elige el personaje 1, después nada
				new ModoJugadorVsMaquinas("Tester", sc).jugar();
			}, 4000);

			if (t instanceof NoSuchElementException) {
				T.finding("IO-EOF-3", "MEDIA",
						"Con el personaje ya elegido, apenas empieza la partida el turno del humano "
								+ "(JugadorHumano.leerOpcionEnRango -> scanner.nextLine()) también corta con "
								+ "NoSuchElementException si no hay más entrada.");
			} else if (t == null) {
				T.fail("se esperaba NoSuchElementException al llegar al turno del humano");
			} else {
				T.fail("se obtuvo " + t.getClass().getName());
			}
		}

		T.section("IO - resumen");
		T.finding("IO-EOF-0", "MEDIA",
				"Ningún punto de lectura (Main, Consola, JugadorHumano, ModoJugadorVsMaquinas) maneja el fin de "
						+ "entrada. Un `hasNextLine()` antes de cada `nextLine()` (o un try/catch que corte prolijo) "
						+ "evitaría todos los IO-EOF-*.");
	}

	static Throwable correrConTimeout(Runnable r, long ms) {
		AtomicReference<Throwable> err = new AtomicReference<>();
		T.mute();
		Thread th = new Thread(() -> {
			try {
				r.run();
			} catch (Throwable t) {
				err.set(t);
			}
		});
		th.setDaemon(true);
		th.start();
		try {
			th.join(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		T.unmute();
		if (th.isAlive()) {
			return new RuntimeException("timeout: no terminó en " + ms + " ms");
		}
		return err.get();
	}
}
