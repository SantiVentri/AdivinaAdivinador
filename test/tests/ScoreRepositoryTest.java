package tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import score.ScoreRepository;

/**
 * OJO: ScoreRepository usa la ruta relativa fija "scores.txt" (directorio actual).
 * Estos tests escriben/borran ese archivo en el CWD. El runner corre en un directorio
 * temporal justamente por esto (ver run-tests). Igual hacemos backup/restore por las dudas.
 */
public class ScoreRepositoryTest {

	private static final Path FILE = Paths.get("scores.txt");

	public static void run() {
		byte[] backup = null;
		boolean existia = Files.exists(FILE);
		try {
			if (existia) {
				backup = Files.readAllBytes(FILE);
			}

			T.section("ScoreRepository - persistencia básica");
			safeDelete();

			ScoreRepository r1 = new ScoreRepository();
			T.eq(0, r1.getVictorias("Santi"), "jugador nuevo -> 0 victorias");
			r1.registrarVictoria("Santi");
			r1.registrarVictoria("Santi");
			r1.registrarVictoria("Lucia");
			T.eq(2, r1.getVictorias("Santi"), "2 victorias de Santi en memoria");

			ScoreRepository r2 = new ScoreRepository(); // relee del archivo
			T.eq(2, r2.getVictorias("Santi"), "otra instancia relee 2 victorias de Santi del archivo");
			T.eq(1, r2.getVictorias("Lucia"), "y 1 de Lucia");

			T.section("ScoreRepository - orden del marcador");
			List<Map.Entry<String, Integer>> ord = r2.obtenerPuntajesOrdenados();
			T.check(ord.get(0).getValue() >= ord.get(ord.size() - 1).getValue(), "ordenado de mayor a menor");
			T.eq("Santi", ord.get(0).getKey(), "Santi primero (2 victorias)");

			T.section("ScoreRepository - nombre con ';' corrompe el archivo");
			safeDelete();
			ScoreRepository r3 = new ScoreRepository();
			r3.registrarVictoria("Ana"); // válido
			r3.registrarVictoria("ro;be"); // Main permite este nombre (5 chars, sin validar el charset)
			r3.registrarVictoria("Zoe"); // válido, se guarda DESPUÉS de la línea corrupta

			String contenido = new String(Files.readAllBytes(FILE));
			T.realOut().println("    (scores.txt =\n" + indent(contenido) + "    )");

			ScoreRepository r4 = new ScoreRepository(); // intenta releer
			if (r4.getVictorias("Ana") == 1 && r4.getVictorias("Zoe") == 1) {
				T.pass("todos los nombres se releyeron bien");
			} else {
				T.finding("SCORE-SEP-1", "MEDIA",
						"Un nombre con ';' rompe el formato 'nombre;n' de scores.txt. Al releer, la línea "
								+ "'ro;be;1' se parte en ['ro','be;1'] y Integer.parseInt('be;1') tira NumberFormatException. "
								+ "El catch está FUERA del while de lectura, así que una sola línea corrupta descarta "
								+ "TODOS los puntajes que vienen después. (releído: Ana=" + r4.getVictorias("Ana")
								+ ", Zoe=" + r4.getVictorias("Zoe") + ", esperado 1 y 1). "
								+ "Main.pedirNombre solo valida el largo (3..12), no los caracteres.");
			}

			T.section("ScoreRepository - archivo con líneas basura");
			safeDelete();
			Files.write(FILE, ("basura sin separador\n"
					+ "Pepe;3\n"
					+ "Otro;noEsNumero\n"
					+ "Depp;5\n").getBytes());
			ScoreRepository r5 = new ScoreRepository();
			T.realOut().println("    Pepe=" + r5.getVictorias("Pepe") + "  Depp=" + r5.getVictorias("Depp"));
			if (r5.getVictorias("Pepe") == 3 && r5.getVictorias("Depp") == 5) {
				T.pass("las líneas válidas se cargan aunque haya basura");
			} else {
				T.finding("SCORE-PARSE-1", "MEDIA",
						"scores.txt con una línea tipo 'Otro;noEsNumero' aborta toda la carga: se pierden las líneas "
								+ "siguientes ('Depp;5'). Cargado: Pepe=" + r5.getVictorias("Pepe")
								+ ", Depp=" + r5.getVictorias("Depp") + ". El try/catch envuelve el while entero.");
			}

			T.section("ScoreRepository - nombre vacío / espacios");
			safeDelete();
			ScoreRepository r6 = new ScoreRepository();
			T.expectNoThrow(() -> r6.registrarVictoria(""), "registrarVictoria(\"\") no rompe");
			T.expectNoThrow(() -> r6.registrarVictoria("   "), "registrarVictoria(\"   \") no rompe");
			ScoreRepository r7 = new ScoreRepository();
			T.realOut().println("    releído nombre vacío = " + r7.getVictorias(""));

			T.section("ScoreRepository - ruta fija, no inyectable");
			T.finding("SCORE-PATH-1", "BAJA",
					"ScoreRepository hardcodea \"scores.txt\" relativo al directorio de ejecución. No se puede testear "
							+ "aislado sin tocar el CWD, y el marcador 'se mueve' según desde dónde se lance el juego "
							+ "(doble click vs consola vs IDE).");

		} catch (IOException e) {
			T.fail("ScoreRepositoryTest IOException: " + e.getMessage());
		} finally {
			try {
				if (backup != null) {
					Files.write(FILE, backup);
				} else {
					safeDelete();
				}
			} catch (IOException ignored) {
			}
		}
	}

	private static void safeDelete() {
		try {
			Files.deleteIfExists(FILE);
		} catch (IOException ignored) {
		}
	}

	private static String indent(String s) {
		StringBuilder sb = new StringBuilder();
		for (String line : s.split("\n", -1)) {
			sb.append("      ").append(line).append("\n");
		}
		return sb.toString();
	}
}
