package score;

import utils.MergeSort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


// Persiste el marcador en scores.txt con una fila de encabezado y una línea por jugador:
// nombre;jugadas;rondas;partidas
public class ScoreRepository implements RepositorioPuntajes {
	private static final String RUTA_ARCHIVO = "scores.txt";
	private static final String SEPARADOR = ";";
	private static final String ENCABEZADO = String.join(SEPARADOR, "nombre", "jugadas", "rondas", "partidas");
	private static final int COLUMNAS = 4;

	private final Map<String, Puntaje> puntajes;

	public ScoreRepository() {
		this.puntajes = new LinkedHashMap<>();
		cargar();
	}

	// Suma una partida jugada al jugador y persiste el cambio en el archivo.
	@Override
	public void registrarPartidaJugada(String nombreJugador) {
		puntajeDe(nombreJugador).sumarPartidaJugada();
		guardar();
	}

	// Suma una ronda ganada al jugador y persiste el cambio en el archivo.
	@Override
	public void registrarRondaGanada(String nombreJugador) {
		puntajeDe(nombreJugador).sumarRonda();
		guardar();
	}

	// Suma una partida ganada (ganó ambas rondas) al jugador y persiste el cambio en el archivo.
	@Override
	public void registrarPartidaGanada(String nombreJugador) {
		puntajeDe(nombreJugador).sumarPartida();
		guardar();
	}

	// Devuelve el marcador ordenado por partidas ganadas y, a igualdad, por rondas ganadas.
	@Override
	public List<Puntaje> obtenerPuntajesOrdenados() {
		List<Puntaje> ordenado = new ArrayList<>(puntajes.values());
		MergeSort.ordenar(
				ordenado,
				Comparator.comparingInt(Puntaje::getPartidasGanadas)
						.thenComparingInt(Puntaje::getRondasGanadas)
						.reversed()
		);
		return ordenado;
	}

	private Puntaje puntajeDe(String nombreJugador) {
		return puntajes.computeIfAbsent(nombreJugador, nombre -> new Puntaje(nombre, 0, 0, 0));
	}

	// Las líneas que no tienen el formato nombre;jugadas;rondas;partidas (el encabezado,
	// o formatos viejos como nombre;victorias o nombre;rondas;partidas) se ignoran.
	private void cargar() {
		if (!Files.exists(Paths.get(RUTA_ARCHIVO))) {
			return;
		}

		try (BufferedReader lector = new BufferedReader(new FileReader(RUTA_ARCHIVO))) {
			String linea;
			while ((linea = lector.readLine()) != null) {
				if (linea.isBlank() || linea.trim().equalsIgnoreCase(ENCABEZADO)) {
					continue;
				}
				String[] partes = linea.split(SEPARADOR);
				if (partes.length != COLUMNAS) {
					continue;
				}
				try {
					int jugadas = Integer.parseInt(partes[1].trim());
					int rondas = Integer.parseInt(partes[2].trim());
					int partidas = Integer.parseInt(partes[3].trim());
					puntajes.put(partes[0], new Puntaje(partes[0], jugadas, rondas, partidas));
				} catch (NumberFormatException e) {
					System.out.println("Línea inválida en el marcador de records: " + linea);
				}
			}
		} catch (IOException e) {
			System.out.println("No se pudo leer el marcador de records: " + e.getMessage());
		}
	}

	private void guardar() {
		try (BufferedWriter escritor = new BufferedWriter(new FileWriter(RUTA_ARCHIVO))) {
			escritor.write(ENCABEZADO);
			escritor.newLine();
			for (Puntaje puntaje : puntajes.values()) {
				escritor.write(String.join(SEPARADOR,
						puntaje.getNombreJugador(),
						String.valueOf(puntaje.getPartidasJugadas()),
						String.valueOf(puntaje.getRondasGanadas()),
						String.valueOf(puntaje.getPartidasGanadas())));
				escritor.newLine();
			}
		} catch (IOException e) {
			System.out.println("No se pudo guardar el marcador de records: " + e.getMessage());
		}
	}
}
