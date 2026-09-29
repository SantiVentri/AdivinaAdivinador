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

public class MaquinaAleatoria extends Jugador {
	private static final double PROB_ARRIESGAR = 0.3;

	private final Random random = new Random();
    private final HistorialConsultas historial;
    private final Registro registro;
    private final List<Filtro> filtros = FiltroFactory.crearFiltros();

    public MaquinaAleatoria(Tablero tablero, HistorialConsultas historial, Registro registro) {
        super("Máquina Aleatoria", tablero);
        this.historial = historial;
        this.registro = registro;
    }

	@Override
	public FiltroAplicado hacerPregunta() {
        TipoFiltro tipo;
        String valor;
        int intentos = 0;

        do {
            Filtro filtro = filtros.get(random.nextInt(filtros.size()));
            String[] valores = filtro.getValores();
            tipo = filtro.getTipo();
            valor = valores[random.nextInt(valores.length)];
            intentos++;
        } while (historial.yaFuePreguntado(getNombre(), FiltroAplicado.clave(tipo, valor)) && intentos < 50);

		registro.registrar("[Máquina Aleatoria] No analizo nada, pregunto al azar: " + tipo + "=" + valor);

		return new FiltroAplicado(tipo, valor);
	}

	@Override
	public Personaje arriesgarPersonaje() {
		List<Personaje> restantes = getTablero().getPersonajesRestantes();

		if (restantes.isEmpty()) {
            registro.registrar("[Máquina Aleatoria] No quedan personajes para arriesgar.");
			return null;
		}

		boolean forzado = getTablero().quedaUnoSolo();
		if (!forzado && random.nextDouble() >= PROB_ARRIESGAR) {
			// Este turno prefiero preguntar al azar en vez de arriesgar.
			return null;
		}

		int indiceRandom = random.nextInt(restantes.size());
		Personaje elegido = restantes.get(indiceRandom);

        registro.registrar("[Máquina Aleatoria] Arriesgo al azar entre " + restantes.size() + " restantes: " + elegido.getNombre());

		return elegido;
	}
}
