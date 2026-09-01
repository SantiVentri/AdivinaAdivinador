package tests;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

import model.FiltroAplicado;
import model.Personaje;
import model.Tablero;
import players.JugadorHumano;
import utils.PersonajeFactory;

/**
 * Mete basura por la entrada a la capa interactiva y verifica que lo único que puede pasar
 * es (a) que resuelva bien, o (b) que corte por fin de entrada (NoSuchElementException).
 * Cualquier otra excepción es un bug.
 *
 * La lista de tokens se arma en runtime (no como literales) para que ningún carácter de
 * control quede "pelado" en el archivo fuente.
 */
public class FuzzInputTest {

	static final String[] BASURA = construirTokens();

	static String[] construirTokens() {
		List<String> t = new ArrayList<>();
		t.add("");
		t.add("abc");
		t.add("0");
		t.add("-1");
		t.add("-999");
		t.add("1.5");
		t.add("1e3");
		t.add("+2");
		t.add("007");
		t.add("99999999999999999999");
		t.add("2147483648");
		t.add("-2147483649");
		t.add("NaN");
		t.add("null");
		t.add("true");
		t.add("veintitres");
		t.add("1;2");
		t.add("uno dos tres");
		t.add("'); DROP TABLE personajes; --");
		t.add("porciento s porciento s");
		t.add(String.valueOf(' '));
		t.add("   ");
		t.add("\t");
		t.add(" 2 ");
		t.add(" \t 3 \t ");
		t.add("\\n\\r\\t"); // barras literales, no saltos reales
		t.add(new String(Character.toChars(0x1F600))); // emoji
		t.add("ñáé"); // ñáé
		t.add("２"); // dígito 2 fullwidth
		t.add(new String(new char[] { 27, 91 }) + "31m"); // ESC + '[' + "31m" (secuencia ANSI)
		t.add("<script>alert(1)</script>");
		t.add(repeat("a", 5000)); // string larguísimo
		return t.toArray(new String[0]);
	}

	static String repeat(String s, int n) {
		StringBuilder sb = new StringBuilder(s.length() * n);
		for (int i = 0; i < n; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	public static void run() {
		List<Personaje> personajes = PersonajeFactory.crearPersonajes();
		Random rnd = new Random(99);

		T.section("Fuzz - JugadorHumano.arriesgarPersonaje con guiones aleatorios de basura");
		int otrasExcepciones = 0;
		int resueltos = 0;
		int eof = 0;

		for (int iter = 0; iter < 4000; iter++) {
			StringBuilder guion = new StringBuilder();
			int lineas = 1 + rnd.nextInt(12);
			for (int i = 0; i < lineas; i++) {
				guion.append(BASURA[rnd.nextInt(BASURA.length)]).append('\n');
			}
			guion.append("2\n"); // 2 = hacer pregunta -> arriesgarPersonaje devuelve null

			Scanner sc = new Scanner(new ByteArrayInputStream(guion.toString().getBytes(StandardCharsets.UTF_8)));
			JugadorHumano j = new JugadorHumano("Fuzz", new Tablero(personajes), sc);
			j.elegirPersonaje(personajes.get(0));

			T.mute();
			Throwable t = T.capture(() -> {
				Personaje r = j.arriesgarPersonaje();
				if (r != null) {
					throw new IllegalStateException("esperaba null tras elegir 'hacer pregunta'");
				}
			});
			T.unmute();

			if (t == null) {
				resueltos++;
			} else if (t instanceof NoSuchElementException) {
				eof++;
			} else {
				otrasExcepciones++;
				T.realOut().println("    " + t.getClass().getName() + " con guion: " + visible(guion.toString()));
			}
		}

		T.realOut().println("    (resueltos=" + resueltos + "  cortes-por-EOF=" + eof + "  otras=" + otrasExcepciones + ")");
		T.check(otrasExcepciones == 0,
				"4000 guiones de basura: ninguna excepción distinta de NoSuchElementException en JugadorHumano");

		T.section("Fuzz - JugadorHumano.hacerPregunta con basura antes de una elección válida");
		int malPregunta = 0;
		for (int iter = 0; iter < 2000; iter++) {
			StringBuilder guion = new StringBuilder();
			int lineas = rnd.nextInt(8);
			for (int i = 0; i < lineas; i++) {
				guion.append(BASURA[rnd.nextInt(BASURA.length)]).append('\n');
			}
			guion.append("1\n1\n");

			Scanner sc = new Scanner(new ByteArrayInputStream(guion.toString().getBytes(StandardCharsets.UTF_8)));
			JugadorHumano j = new JugadorHumano("Fuzz", new Tablero(personajes), sc);
			j.elegirPersonaje(personajes.get(0));

			T.mute();
			Throwable t = T.capture(() -> {
				FiltroAplicado f = j.hacerPregunta();
				if (f == null || f.getTipo() == null || f.getValor() == null) {
					throw new IllegalStateException("filtro inválido");
				}
			});
			T.unmute();

			if (t != null && !(t instanceof NoSuchElementException)) {
				malPregunta++;
				T.realOut().println("    " + t.getClass().getName() + " :: " + visible(guion.toString()));
			}
		}
		T.check(malPregunta == 0, "2000 guiones: hacerPregunta siempre arma un filtro válido o corta por EOF");

		T.section("Fuzz - selección de personaje secreto (ModoJugadorVsMaquinas) con enteros extremos");
		int malSeleccion = 0;
		for (int iter = 0; iter < 500; iter++) {
			StringBuilder guion = new StringBuilder();
			guion.append(-rnd.nextInt(1000)).append('\n');
			guion.append(24 + rnd.nextInt(100000)).append('\n');
			guion.append("0\n");
			guion.append(BASURA[rnd.nextInt(BASURA.length)]).append('\n');
			guion.append(1 + rnd.nextInt(23)).append('\n');
			guion.append(T.repeatToken("2\n1\n1\n", 60));

			Throwable t = runModoSeleccion(guion.toString());
			if (t != null && !(t instanceof NoSuchElementException)) {
				malSeleccion++;
				T.realOut().println("    " + t);
			}
		}
		T.check(malSeleccion == 0, "500 guiones con enteros extremos en la selección de personaje: sin excepciones raras");
	}

	static String visible(String s) {
		StringBuilder sb = new StringBuilder();
		int shown = 0;
		for (int i = 0; i < s.length() && shown < 200; i++, shown++) {
			char c = s.charAt(i);
			if (c == '\n') {
				sb.append("\\n");
			} else if (c < 0x20) {
				sb.append("<").append((int) c).append(">");
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	static Throwable runModoSeleccion(String guion) {
		java.io.InputStream inReal = System.in;
		java.util.concurrent.atomic.AtomicReference<Throwable> err = new java.util.concurrent.atomic.AtomicReference<>();
		T.mute();
		System.setIn(T.repeating(guion));
		Thread th = new Thread(() -> {
			try {
				new game.ModoJugadorVsMaquinas("Fuzz", new Scanner(System.in)).jugar();
			} catch (Throwable t) {
				err.set(t);
			}
		});
		th.setDaemon(true);
		th.start();
		try {
			th.join(10000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		System.setIn(inReal);
		T.unmute();
		if (th.isAlive()) {
			return new RuntimeException("timeout");
		}
		return err.get();
	}
}
