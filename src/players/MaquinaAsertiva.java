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
		double mitad = restantes / 2.0;

		System.out.println("[Máquina Asertiva] Analizando filtros sobre " + restantes + " personaje(s) restante(s)...");

		FiltroAplicado mejorFiltro = null;
		double mejorDiferencia = Double.MAX_VALUE;

		for (TipoFiltro tipo : TipoFiltro.values()) {
			for (String valor : valoresPosibles(tipo)) {
				if (historial.yaFuePreguntado(clave(tipo, valor))) {
					continue;
				}

				int cantidad = getTablero().contarSiSeAplicara(tipo, valor);
				double diferencia = Math.abs(cantidad - mitad);

				System.out.println("  - Evalúo " + tipo + "=" + valor + " -> " + cantidad + "/" + restantes
						+ " cumplen (diferencia con la mitad: " + diferencia + ")");

				if (diferencia < mejorDiferencia) {
					mejorDiferencia = diferencia;
					mejorFiltro = new FiltroAplicado(tipo, valor);
				}
			}
		}

		if (mejorFiltro != null) {
			System.out.println("[Máquina Asertiva] Elijo " + mejorFiltro.getTipo() + "=" + mejorFiltro.getValor()
					+ " por ser la división más equilibrada.");
		} else {
			System.out.println("[Máquina Asertiva] No me quedan filtros nuevos para probar.");
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

		int indiceRandom = random.nextInt(restantes.size());
		Personaje elegido = restantes.get(indiceRandom);

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

	private String clave(TipoFiltro tipo, String valor) {
		return tipo.name() + "=" + valor;
	}

}
