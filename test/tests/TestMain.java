package tests;

/**
 * Punto de entrada del harness de robustez.
 *
 *   javac -encoding UTF-8 -d out  (todos los .java de src y test)
 *   java  -cp out  tests.TestMain
 *
 * Conviene correrlo desde un directorio temporal: ScoreRepositoryTest escribe "scores.txt"
 * en el directorio actual (ver run-tests.ps1 / run-tests.sh).
 *
 * Exit code 0 si no hay FAILs (aserciones rojas). Los "findings" catalogados no cambian el
 * exit code: son defectos ya confirmados que se documentan en INFORME_ROBUSTEZ.md.
 */
public class TestMain {

	public static void main(String[] args) {
		long inicio = System.currentTimeMillis();
		T.realOut().println("###########################################");
		T.realOut().println("#  AdivinaAdivinador - harness de robustez #");
		T.realOut().println("###########################################");

		safe("PersonajeTest", PersonajeTest::run);
		safe("TableroTest", TableroTest::run);
		safe("FiltroYHistorialTest", FiltroYHistorialTest::run);
		safe("PersonajeFactoryTest", PersonajeFactoryTest::run);
		safe("ScoreRepositoryTest", ScoreRepositoryTest::run);
		safe("JugadorHumanoTest", JugadorHumanoTest::run);
		safe("MotorJuegoTest", MotorJuegoTest::run);
		safe("MaquinasTest", MaquinasTest::run);
		safe("FuzzInputTest", FuzzInputTest::run);
		safe("ModoJugadorVsMaquinasTest", ModoJugadorVsMaquinasTest::run);
		safe("SimulacionTest", SimulacionTest::run);
		safe("IoRobustezTest", IoRobustezTest::run);
		safe("SmokeMainTest", SmokeMainTest::run);

		int code = T.summary();
		long ms = System.currentTimeMillis() - inicio;
		T.realOut().println("tiempo: " + ms + " ms");
		System.exit(code);
	}

	private static void safe(String nombre, Runnable r) {
		try {
			r.run();
		} catch (Throwable t) {
			T.realOut().println();
			T.realOut().println("!!! " + nombre + " abortó con " + t.getClass().getName() + ": " + t.getMessage());
			for (StackTraceElement e : t.getStackTrace()) {
				T.realOut().println("      at " + e);
				if (e.getClassName().startsWith("tests.")) {
					// alcanza con las primeras líneas relevantes
				}
			}
			T.fail("(" + nombre + ") la clase de test lanzó una excepción no controlada: " + t);
		}
	}
}
