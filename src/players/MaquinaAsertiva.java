package players;

import java.util.List;
import java.util.Random;
import model.Filtro;
import model.FiltroAplicado;
import model.Personaje;
import model.Tablero;
import model.TipoFiltro;
import utils.FiltroFactory;
import utils.Registro;

public class MaquinaAsertiva extends JugadorMaquina {
	private final Random random = new Random();
    private final HistorialConsultas historial;
    private final Registro registro;
    private final List<Filtro> filtros = FiltroFactory.crearFiltros();

    public MaquinaAsertiva(Tablero tablero, HistorialConsultas historial, Registro registro) {
        super("Máquina Asertiva", tablero);
        this.historial = historial;
        this.registro = registro;
    }

	@Override
	public FiltroAplicado hacerPregunta() {
		int restantes = getTablero().cantidadRestante();
		registro.registrar("[Máquina Asertiva] Analizando filtros sobre " + restantes + " personaje(s) restante(s)...");

		FiltroAplicado mejorFiltro = buscarMejorFiltro(true);

		if (mejorFiltro != null) {
			registro.registrar("[Máquina Asertiva] Elijo " + mejorFiltro.getTipo().toString().replace("_", " ") + "=" + mejorFiltro.getValor().toLowerCase()
					+ " por ser la división más equilibrada.");
		} else {
			registro.registrar("[Máquina Asertiva] No me quedan filtros nuevos para probar.");
		}

		return mejorFiltro;
	}

	// Elige el filtro (no preguntado aún) cuya cantidad de coincidencias esté más cerca de la
	// mitad de los personajes restantes (máxima división). Devuelve null si no queda ninguno.
	private FiltroAplicado buscarMejorFiltro(boolean verboso) {
		double mitad = getTablero().cantidadRestante() / 2.0;
		FiltroAplicado mejorFiltro = null;
		double mejorDiferencia = Double.MAX_VALUE;

        for (Filtro filtro : filtros) {
            TipoFiltro tipo = filtro.getTipo();
            for (String valor : filtro.getValores()) {
				if (historial.yaFuePreguntado(getNombre(), FiltroAplicado.clave(tipo, valor))) {
					continue;
				}

				int cantidad = getTablero().contarSiSeAplicara(tipo, valor);
				double diferencia = Math.abs(cantidad - mitad);

				if (verboso) {
					registro.registrar("  - Evalúo " + tipo.toString().replace("_", " ") + " = " + valor + " -> " + cantidad
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
			registro.registrar("[Máquina Asertiva] No quedan personajes para arriesgar.");
			return null;
		}

		// Solo arriesga cuando ya no puede seguir descartando: queda uno solo,
		// o no le quedan preguntas nuevas para hacer.
		boolean sinPreguntas = buscarMejorFiltro(false) == null;
		if (!getTablero().quedaUnoSolo() && !sinPreguntas) {
			return null;
		}

		Personaje elegido = restantes.get(random.nextInt(restantes.size()));
		registro.registrar("[Máquina Asertiva] Arriesgo entre " + restantes.size() + " restante(s): " + elegido.getNombre());

		return elegido;
	}

}
