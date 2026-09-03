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

public class MaquinaAleatoria extends Jugador {
	private static final double PROB_ARRIESGAR = 0.3;

	private final Random random = new Random();
	private final HistorialConsultas historial;

	public MaquinaAleatoria(Tablero tablero, HistorialConsultas historial) {
		super("Máquina Aleatoria", tablero);
		this.historial = historial;
	}

	@Override
	public FiltroAplicado hacerPregunta() {
		TipoFiltro[] tipos = TipoFiltro.values();
		TipoFiltro tipo;
		String valor;
		int intentos = 0;

		do {
			tipo = tipos[random.nextInt(tipos.length)];
			valor = elegirValorAlAzar(tipo);
			intentos++;
		} while (historial.yaFuePreguntado(getNombre(), FiltroAplicado.clave(tipo, valor)) && intentos < 50);

		System.out.println("[Máquina Aleatoria] No analizo nada, pregunto al azar: " + tipo + "=" + valor);

		return new FiltroAplicado(tipo, valor);
	}

	@Override
	public Personaje arriesgarPersonaje() {
		List<Personaje> restantes = getTablero().getPersonajesRestantes();

		if (restantes.isEmpty()) {
			System.out.println("[Máquina Aleatoria] No quedan personajes para arriesgar.");
			return null;
		}

		boolean forzado = getTablero().quedaUnoSolo();
		if (!forzado && random.nextDouble() >= PROB_ARRIESGAR) {
			// Este turno prefiero preguntar al azar en vez de arriesgar.
			return null;
		}

		int indiceRandom = random.nextInt(restantes.size());
		Personaje elegido = restantes.get(indiceRandom);

		System.out.println("[Máquina Aleatoria] Arriesgo al azar entre " + restantes.size() + " restantes: " + elegido.getNombre());

		return elegido;
	}

	private String elegirValorAlAzar(TipoFiltro tipo) {
		switch (tipo) {
			case GENERO:
				return valorAlAzar(Genero.values());
			case EDAD:
				return valorAlAzar(Edad.values());
			case COLOR_PELO:
				return valorAlAzar(ColorPelo.values());
			case CASA_HOGWARTS:
				return valorAlAzar(CasaHogwarts.values());
			case SANGRE_LIMPIA:
				return valorAlAzar(SangreLimpia.values());
			case CALVICIE:
			case LENTES:
			case ALUMNO:
				return String.valueOf(random.nextBoolean());
			default:
				throw new IllegalStateException("Tipo de filtro no soportado: " + tipo);
		}
	}

	private <T extends Enum<T>> String valorAlAzar(T[] valores) {
		return valores[random.nextInt(valores.length)].name();
	}

}
