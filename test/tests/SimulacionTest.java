package tests;

import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import game.ModoMaquinaVsMaquina;
import game.MotorJuego;
import model.Personaje;
import model.Tablero;
import players.HistorialConsultas;
import players.Jugador;
import players.MaquinaAleatoria;
import players.MaquinaAsertiva;
import utils.PersonajeFactory;

/**
 * Simulaciones masivas: tirar miles de partidas de máquina contra máquina para intentar
 * romper el motor (excepciones, cuelgues, empates raros, partidas eternas).
 */
public class SimulacionTest {

	static final int PARTIDAS_MOTOR = 4000;
	static final int PARTIDAS_MODO = 120;
	static final long TIMEOUT_MS = 4000;

	public static void run() {
		Random rnd = new Random(12345); // semilla del test (no de las máquinas, que usan su propio Random)

		T.section("Simulación - " + PARTIDAS_MOTOR + " partidas MotorJuego (Asertiva vs Aleatoria)");

		int excepciones = 0;
		int cuelgues = 0;
		int empates = 0;
		int ganaAsertiva = 0;
		int ganaAleatoria = 0;
		int maxPreguntas = 0;
		long sumaPreguntas = 0;
		String peorCaso = "";

		T.mute();
		for (int i = 0; i < PARTIDAS_MOTOR; i++) {
			List<Personaje> base = PersonajeFactory.crearPersonajes();
			HistorialConsultas h = new HistorialConsultas();

			MaquinaAsertiva asertiva = new MaquinaAsertiva(new Tablero(base), h);
			MaquinaAleatoria aleatoria = new MaquinaAleatoria(new Tablero(base), h);
			Personaje secA = base.get(rnd.nextInt(base.size()));
			Personaje secB = base.get(rnd.nextInt(base.size()));
			asertiva.elegirPersonaje(secA);
			aleatoria.elegirPersonaje(secB);

			boolean asertivaPrimero = rnd.nextBoolean();
			Jugador j1 = asertivaPrimero ? asertiva : aleatoria;
			Jugador j2 = asertivaPrimero ? aleatoria : asertiva;

			MotorJuego motor = new MotorJuego(j1, j2, h);

			AtomicReference<Jugador> res = new AtomicReference<>();
			AtomicReference<Throwable> err = new AtomicReference<>();
			Thread th = new Thread(() -> {
				try {
					res.set(motor.jugar());
				} catch (Throwable t) {
					err.set(t);
				}
			});
			th.setDaemon(true);
			th.start();
			joinQuiet(th, TIMEOUT_MS);

			if (th.isAlive()) {
				cuelgues++;
				T.unmute();
				T.realOut().println("    CUELGUE en partida " + i + " (secA=" + secA.getNombre()
						+ ", secB=" + secB.getNombre() + ", asertivaPrimero=" + asertivaPrimero + ")");
				T.mute();
				if (cuelgues >= 3) {
					break;
				}
				continue;
			}
			if (err.get() != null) {
				excepciones++;
				T.unmute();
				T.realOut().println("    EXCEPCIÓN en partida " + i + ": " + err.get());
				T.mute();
				if (excepciones >= 3) {
					break;
				}
				continue;
			}

			Jugador g = res.get();
			int preguntas = h.getCantidadConsultas();
			sumaPreguntas += preguntas;
			if (preguntas > maxPreguntas) {
				maxPreguntas = preguntas;
				peorCaso = "secA=" + secA.getNombre() + ", secB=" + secB.getNombre();
			}
			if (g == null) {
				empates++;
			} else if (g == asertiva) {
				ganaAsertiva++;
			} else {
				ganaAleatoria++;
			}
		}
		T.unmute();

		int jugadas = ganaAsertiva + ganaAleatoria + empates;
		T.realOut().println("    resultados: asertiva=" + ganaAsertiva + "  aleatoria=" + ganaAleatoria
				+ "  empates=" + empates + "  excepciones=" + excepciones + "  cuelgues=" + cuelgues);
		T.realOut().println("    preguntas por partida: max=" + maxPreguntas
				+ " (" + peorCaso + ")  prom=" + (jugadas == 0 ? 0 : sumaPreguntas / jugadas));

		T.check(excepciones == 0, "ninguna partida MotorJuego lanzó excepción");
		T.check(cuelgues == 0, "ninguna partida MotorJuego se colgó (> " + TIMEOUT_MS + " ms)");

		if (empates > 0) {
			T.finding("SIM-EMPATE-1", "BAJA",
					empates + "/" + jugadas + " partidas terminaron en 'empate' (ambos tableros vacíos). "
							+ "Pasa cuando las dos máquinas arriesgan mal hasta vaciar su tablero. El motor lo maneja "
							+ "(devuelve null) pero conviene saber que ocurre.");
		} else {
			T.pass("no hubo empates (ambos tableros vacíos) en " + jugadas + " partidas");
		}

		if (maxPreguntas > 250) {
			T.finding("SIM-LARGO-1", "BAJA",
					"Hubo una partida con " + maxPreguntas + " preguntas. Sin tope de turnos, contra un humano que "
							+ "responde lento esto puede volverse tedioso; con máquinas termina igual.");
		} else {
			T.pass("la partida más larga tuvo " + maxPreguntas + " preguntas (acotado)");
		}

		if (ganaAsertiva <= ganaAleatoria) {
			T.finding("SIM-ESTRAT-1", "MEDIA",
					"La 'Máquina Asertiva' NO le gana claramente a la 'Máquina Aleatoria' (asertiva=" + ganaAsertiva
							+ " vs aleatoria=" + ganaAleatoria + "). Si la estrategia asertiva no rinde mejor que el azar, "
							+ "hay algo flojo en su heurística o en cuándo decide arriesgar.");
		} else {
			T.pass("la asertiva le gana a la aleatoria (" + ganaAsertiva + " vs " + ganaAleatoria + ")");
		}

		T.section("Simulación - " + PARTIDAS_MODO + " corridas de ModoMaquinaVsMaquina (integración real)");

		InputStream inReal = System.in;
		System.setIn(T.infiniteNewlines()); // el modo hace 'Presioná Enter' tras cada turno
		int modoExc = 0;
		int modoCuelga = 0;
		try {
			T.mute();
			for (int i = 0; i < PARTIDAS_MODO; i++) {
				AtomicReference<Throwable> err = new AtomicReference<>();
				Thread th = new Thread(() -> {
					try {
						new ModoMaquinaVsMaquina(new java.util.Scanner(System.in)).jugar();
					} catch (Throwable t) {
						err.set(t);
					}
				});
				th.setDaemon(true);
				th.start();
				joinQuiet(th, 6000);
				if (th.isAlive()) {
					modoCuelga++;
					if (modoCuelga >= 2) {
						break;
					}
				} else if (err.get() != null) {
					modoExc++;
					T.unmute();
					T.realOut().println("    EXCEPCIÓN en ModoMaquinaVsMaquina corrida " + i + ": " + err.get());
					T.mute();
					if (modoExc >= 2) {
						break;
					}
				}
			}
			T.unmute();
		} finally {
			System.setIn(inReal);
		}

		T.check(modoExc == 0, "ninguna corrida de ModoMaquinaVsMaquina lanzó excepción");
		T.check(modoCuelga == 0, "ninguna corrida de ModoMaquinaVsMaquina se colgó");
	}

	static void joinQuiet(Thread th, long ms) {
		try {
			th.join(ms);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
