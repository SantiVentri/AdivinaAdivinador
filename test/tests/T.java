package tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Mini harness de tests sin dependencias. Se corre con `java tests.TestMain`.
 *
 *  - check / eq / expectThrows / expectNoThrow: aserciones normales.
 *      verde = comportamiento correcto, rojo = problema. El exit code depende de estas.
 *  - finding(id, sev, detalle): cataloga un defecto YA confirmado (el test muestra el
 *      comportamiento roto a propósito). No afecta el exit code: sirve para que la corrida
 *      sea estable y el informe se arme solo desde la lista de findings.
 */
public final class T {

	public static int checks = 0;
	public static int passed = 0;
	public static int failed = 0;

	public static final List<String> failLog = new ArrayList<>();
	public static final List<String> findingLog = new ArrayList<>();

	private static String current = "?";
	private static final PrintStream REAL_OUT = System.out;
	private static PrintStream muted;

	private T() {
	}

	public static void section(String name) {
		current = name;
		REAL_OUT.println();
		REAL_OUT.println("==== " + name + " ====");
	}

	public static void pass(String msg) {
		checks++;
		passed++;
		REAL_OUT.println("  ok    " + msg);
	}

	public static void fail(String msg) {
		checks++;
		failed++;
		String line = "[FAIL] (" + current + ") " + msg;
		failLog.add(line);
		REAL_OUT.println("  FAIL  " + msg);
	}

	public static void check(boolean cond, String msg) {
		if (cond) {
			pass(msg);
		} else {
			fail(msg);
		}
	}

	public static void eq(Object expected, Object actual, String msg) {
		boolean ok = (expected == null) ? actual == null : expected.equals(actual);
		if (ok) {
			pass(msg + "  [= " + expected + "]");
		} else {
			fail(msg + "  esperado <" + expected + "> obtenido <" + actual + ">");
		}
	}

	/** Defecto confirmado que se cataloga (no rompe la corrida). sev: ALTA / MEDIA / BAJA. */
	public static void finding(String id, String sev, String detalle) {
		String line = "[" + sev + "] " + id + " (" + current + ") :: " + detalle;
		findingLog.add(line);
		REAL_OUT.println("  FIND  " + sev + " " + id + " :: " + detalle);
	}

	public interface Block {
		void run() throws Throwable;
	}

	public static Throwable capture(Block b) {
		PrintStream prev = System.out;
		try {
			b.run();
			return null;
		} catch (Throwable t) {
			return t;
		} finally {
			System.setOut(prev);
		}
	}

	public static void expectThrows(Class<? extends Throwable> type, Block b, String msg) {
		Throwable t = capture(b);
		if (t == null) {
			fail(msg + "  (no lanzó nada; se esperaba " + type.getSimpleName() + ")");
		} else if (type.isInstance(t)) {
			pass(msg + "  (lanzó " + t.getClass().getSimpleName() + ")");
		} else {
			fail(msg + "  lanzó " + t.getClass().getName() + " en vez de " + type.getSimpleName());
		}
	}

	public static void expectNoThrow(Block b, String msg) {
		Throwable t = capture(b);
		if (t == null) {
			pass(msg);
		} else {
			fail(msg + "  lanzó " + t.getClass().getName() + ": " + t.getMessage());
		}
	}

	// ---- utilidades de I/O ----

	/** Silencia System.out mientras corre el bloque (para código muy verboso). */
	public static void mute() {
		if (muted == null) {
			muted = new PrintStream(new OutputStream() {
				@Override
				public void write(int b) {
				}
			});
		}
		System.setOut(muted);
	}

	public static void unmute() {
		System.setOut(REAL_OUT);
	}

	public static PrintStream realOut() {
		return REAL_OUT;
	}

	public static InputStream stdin(String s) {
		return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	}

	/** Stream que devuelve '\n' para siempre: evita el EOF al probar loops interactivos. */
	public static InputStream infiniteNewlines() {
		return new InputStream() {
			@Override
			public int read() {
				return '\n';
			}
		};
	}

	/** Repite un guion de texto para siempre (nunca hay EOF). Útil para partidas de largo variable. */
	public static InputStream repeating(String guion) {
		final byte[] bytes = guion.getBytes(StandardCharsets.UTF_8);
		return new InputStream() {
			private int i = 0;

			@Override
			public int read() {
				int b = bytes[i] & 0xFF;
				i = (i + 1) % bytes.length;
				return b;
			}
		};
	}

	public static String repeatToken(String s, int veces) {
		StringBuilder sb = new StringBuilder(s.length() * veces);
		for (int i = 0; i < veces; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	public static int summary() {
		REAL_OUT.println();
		REAL_OUT.println("================ RESUMEN ================");
		REAL_OUT.println("checks: " + checks + "   ok: " + passed + "   FAIL: " + failed);
		REAL_OUT.println("findings catalogados: " + findingLog.size());
		if (!failLog.isEmpty()) {
			REAL_OUT.println();
			REAL_OUT.println("-- FAILS (aserciones rojas) --");
			for (String s : failLog) {
				REAL_OUT.println("  " + s);
			}
		}
		if (!findingLog.isEmpty()) {
			REAL_OUT.println();
			REAL_OUT.println("-- FINDINGS (defectos confirmados) --");
			for (String s : findingLog) {
				REAL_OUT.println("  " + s);
			}
		}
		REAL_OUT.println("========================================");
		return failed == 0 ? 0 : 1;
	}
}
