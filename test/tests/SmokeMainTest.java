package tests;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import main.Main;

/**
 * Smoke test de la app entera por Main.main.
 *
 * OJO: Main tiene un `Scanner` estático que se crea UNA sola vez, al cargar la clase,
 * tomando el System.in de ese momento. Por eso este test corre UNA sola vez por JVM y
 * hay que fijar System.in ANTES de tocar Main.
 */
public class SmokeMainTest {

	public static void run() {
		T.section("SmokeMain - navegar menús y salir sin jugar");

		// ab (corto) -> abcdefghijklmno (largo, 15) -> Santi (ok)
		// menú: 1 (jugar modos) -> 3 (volver) -> 2 (ver puntajes) -> 3 (salir)
		String guion = "ab\nabcdefghijklmno\nSanti\n1\n3\n2\n3\n";

		InputStream inReal = System.in;
		PrintStream outReal = System.out;
		ByteArrayOutputStream capturado = new ByteArrayOutputStream();

		System.setIn(new java.io.ByteArrayInputStream(guion.getBytes(StandardCharsets.UTF_8)));
		System.setOut(new PrintStream(capturado, true, StandardCharsets.UTF_8));

		AtomicReference<Throwable> err = new AtomicReference<>();
		Thread th = new Thread(() -> {
			try {
				Main.main(new String[0]);
			} catch (Throwable t) {
				err.set(t);
			}
		});
		th.setDaemon(true);
		th.start();
		try {
			th.join(8000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		System.setIn(inReal);
		System.setOut(outReal);

		String salida = capturado.toString(StandardCharsets.UTF_8);

		if (th.isAlive()) {
			T.fail("Main.main no terminó en 8s con un guion que pide salir (posible cuelgue o lectura de más)");
		} else if (err.get() != null) {
			T.fail("Main.main lanzó " + err.get().getClass().getName() + ": " + err.get().getMessage());
		} else {
			T.pass("Main.main navegó los menús y terminó sin excepción");
		}

		T.check(salida.contains("Bienvenido a AdivinaAdivinador"), "imprimió el banner de bienvenida");
		T.check(salida.contains("mínimo 3 letras"), "rechazó el nombre corto 'ab'");
		T.check(salida.contains("menos de 12 letras"), "rechazó el nombre largo de 15");
		T.check(salida.contains("Gracias por jugar"), "llegó a la despedida (salida limpia)");

		// El mensaje del límite superior dice 'menos de 12' pero el chequeo es '> 12' (12 se acepta).
		T.finding("MAIN-MSG-1", "BAJA",
				"Main.pedirNombre: el chequeo de largo máximo es `> 12` (12 letras se aceptan) pero el mensaje dice "
						+ "'menos de 12 letras'. Inconsistencia menor de mensaje.");

		T.section("SmokeMain - nota sobre testabilidad");
		T.finding("MAIN-STATIC-1", "BAJA",
				"El `Scanner` estático de Main atado a System.in impide testear Main más de una vez por proceso y "
						+ "obliga a fijar System.in antes de cargar la clase. Recibir el Scanner por parámetro "
						+ "lo haría testeable de punta a punta.");
	}
}
