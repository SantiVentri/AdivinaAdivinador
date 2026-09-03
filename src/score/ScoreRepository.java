package score;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ScoreRepository implements RepositorioPuntajes {
	private static final String RUTA_ARCHIVO = "scores.txt";
	private static final String SEPARADOR = ";";

	private final Map<String, Integer> puntajes;

	public ScoreRepository() {
		this.puntajes = new LinkedHashMap<>();
		cargar();
	}

	// Suma una partida ganada al jugador y persiste el cambio en el archivo.
	@Override
	public void registrarVictoria(String nombreJugador) {
		int actuales = puntajes.getOrDefault(nombreJugador, 0);
		puntajes.put(nombreJugador, actuales + 1);
		guardar();
	}

	@Override
	public int getVictorias(String nombreJugador) {
		return puntajes.getOrDefault(nombreJugador, 0);
	}

	// Devuelve el marcador ordenado de mayor a menor cantidad de victorias.
	@Override
	public List<Map.Entry<String, Integer>> obtenerPuntajesOrdenados() {
		List<Map.Entry<String, Integer>> ordenado = new ArrayList<>(puntajes.entrySet());
		ordenado.sort((a, b) -> b.getValue() - a.getValue());
		return ordenado;
	}

	@Override
	public void mostrar() {
		if (puntajes.isEmpty()) {
			System.out.println("\n(Todavía no hay puntajes registrados.)");
			return;
		}

		System.out.println("\n---------- Marcador de records ----------");
		int puesto = 1;
		for (Map.Entry<String, Integer> entrada : obtenerPuntajesOrdenados()) {
			System.out.println(puesto + ". " + entrada.getKey() + " - " + entrada.getValue() + " victoria(s)");
			puesto++;
		}
	}

	private void cargar() {
		if (!Files.exists(Paths.get(RUTA_ARCHIVO))) {
			return;
		}

		try (BufferedReader lector = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
			String linea;
			while ((linea = lector.readLine()) != null) {
				if (linea.isBlank()) {
					continue;
				}
				String[] partes = linea.split(SEPARADOR, 2);
				if (partes.length == 2) {
					puntajes.put(partes[0], Integer.valueOf(partes[1].trim()));
				}
			}
		} catch (IOException | NumberFormatException e) {
			System.out.println("No se pudo leer el marcador de records: " + e.getMessage());
		}
	}

	private void guardar() {
		try (BufferedWriter escritor = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
			for (Map.Entry<String, Integer> entrada : puntajes.entrySet()) {
				escritor.write(entrada.getKey() + SEPARADOR + entrada.getValue());
				escritor.newLine();
			}
		} catch (IOException e) {
			System.out.println("No se pudo guardar el marcador de records: " + e.getMessage());
		}
	}
}
