package players;

import java.util.List;
import java.util.Random;
import model.CasaHogwarts;
import model.ColorPelo;
import model.Edad;
import model.FiltroAplicado;
import model.Genero;
import model.Personaje;
import model.SangreLimpia;
import model.Tablero;
import model.TipoFiltro;

public class MaquinaAsertiva extends Jugador {
	private final Random random = new Random();
	private final HistorialConsultas historial;

	public MaquinaAsertiva(Tablero tablero, HistorialConsultas historial) {
		super("Máquina Asertiva", tablero);
		this.historial = historial;
	}

	@Override
	public FiltroAplicado hacerPregunta() {
		int restantes = getTablero().cantidadRestante();
		System.out.println("[Máquina Asertiva] Analizando filtros sobre " + restantes + " personaje(s) restante(s)...");

		FiltroAplicado mejorFiltro = buscarMejorFiltro(true);

		if (mejorFiltro != null) {
			System.out.println("[Máquina Asertiva] Elijo " + mejorFiltro.getTipo().toString().replace("_", " ") + "=" + mejorFiltro.getValor().toLowerCase()
					+ " por ser la división más equilibrada.");
		} else {
			System.out.println("[Máquina Asertiva] No me quedan filtros nuevos para probar.");
		}

		return mejorFiltro;
	}

	// Elige el filtro (no preguntado aún) cuya cantidad de coincidencias esté más cerca de la
	// mitad de los personajes restantes (máxima división). Devuelve null si no queda ninguno.
	private FiltroAplicado buscarMejorFiltro(boolean verboso) {
		double mitad = getTablero().cantidadRestante() / 2.0;
		FiltroAplicado mejorFiltro = null;
		double mejorDiferencia = Double.MAX_VALUE;

		for (TipoFiltro tipo : TipoFiltro.values()) {
			for (String valor : valoresPosibles(tipo)) {
				if (historial.yaFuePreguntado(getNombre(), FiltroAplicado.clave(tipo, valor))) {
					continue;
				}

				int cantidad = getTablero().contarSiSeAplicara(tipo, valor);
				double diferencia = Math.abs(cantidad - mitad);

				if (verboso) {
					System.out.println("  - Evalúo " + tipo.toString().replace("_", " ") + " = " + valor + " -> " + cantidad
							+ " cumplen (diferencia con la mitad: " + diferencia + ")");
				}

				if (diferencia < mejorDiferencia) {
					mejorDiferencia = diferencia;
					mejorFiltro = new FiltroAplicado(tipo, valor);
				}
			}
		}

		return mejorFiltro;
	}

	@Override
	public Personaje arriesgarPersonaje() {
		List<Personaje> restantes = getTablero().getPersonajesRestantes();

		if (restantes.isEmpty()) {
			System.out.println("[Máquina Asertiva] No quedan personajes para arriesgar.");
			return null;
		}

		// Solo arriesga cuando ya no puede seguir descartando: queda uno solo,
		// o no le quedan preguntas nuevas para hacer.
		boolean sinPreguntas = buscarMejorFiltro(false) == null;
		if (!getTablero().quedaUnoSolo() && !sinPreguntas) {
			return null;
		}

		Personaje elegido = restantes.get(random.nextInt(restantes.size()));
		System.out.println("[Máquina Asertiva] Arriesgo entre " + restantes.size() + " restante(s): " + elegido.getNombre());

		return elegido;
	}

	private String[] valoresPosibles(TipoFiltro tipo) {
		switch (tipo) {
			case GENERO:
				return nombresDe(Genero.values());
			case EDAD:
				return nombresDe(Edad.values());
			case COLOR_PELO:
				return nombresDe(ColorPelo.values());
			case CASA_HOGWARTS:
				return nombresDe(CasaHogwarts.values());
			case SANGRE_LIMPIA:
				return nombresDe(SangreLimpia.values());
			case CALVICIE:
			case LENTES:
			case ALUMNO:
				return new String[] { "true", "false" };
			default:
				throw new IllegalStateException("Tipo de filtro no soportado: " + tipo);
		}
	}

	private <T extends Enum<T>> String[] nombresDe(T[] valores) {
		String[] nombres = new String[valores.length];
		for (int i = 0; i < valores.length; i++) {
			nombres[i] = valores[i].name();
		}
		return nombres;
	}

}
