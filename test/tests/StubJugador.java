package tests;

import java.util.ArrayList;
import java.util.List;

import model.FiltroAplicado;
import model.Personaje;
import model.Tablero;
import players.Jugador;

/**
 * Jugador de laboratorio: se le programa qué hacer en cada turno para poder
 * dirigir el MotorJuego de forma determinística.
 */
public class StubJugador extends Jugador {

	public interface Turno {
		/** Devuelve un Personaje para arriesgar, o null. */
		Personaje arriesgar();

		/** Devuelve un FiltroAplicado para preguntar, o null. */
		FiltroAplicado preguntar();
	}

	public int turnosJugados = 0;
	public int preguntasHechas = 0;
	public int arriesgadas = 0;
	public final List<String> vistos = new ArrayList<>();

	private final List<Turno> guion = new ArrayList<>();
	private Turno porDefecto = new Turno() {
		public Personaje arriesgar() {
			return null;
		}

		public FiltroAplicado preguntar() {
			return null;
		}
	};
	private int maxTurnos = -1;

	public StubJugador(String nombre, Tablero tablero) {
		super(nombre, tablero);
	}

	public StubJugador programar(Turno... turnos) {
		for (Turno t : turnos) {
			guion.add(t);
		}
		return this;
	}

	public StubJugador porDefecto(Turno t) {
		this.porDefecto = t;
		return this;
	}

	/** Si el motor pide más de `n` turnos, lanza AssertionError (para cazar bucles infinitos). */
	public StubJugador topeDeTurnos(int n) {
		this.maxTurnos = n;
		return this;
	}

	private Turno siguiente() {
		turnosJugados++;
		vistos.add("turno" + turnosJugados);
		if (maxTurnos > 0 && turnosJugados > maxTurnos) {
			throw new AssertionError("El motor pidió más de " + maxTurnos
					+ " turnos a " + getNombre() + " sin terminar la partida (posible bucle infinito).");
		}
		if (turnosJugados <= guion.size()) {
			return guion.get(turnosJugados - 1);
		}
		return porDefecto;
	}

	@Override
	public Personaje arriesgarPersonaje() {
		Personaje p = siguiente().arriesgar();
		if (p != null) {
			arriesgadas++;
		}
		return p;
	}

	@Override
	public FiltroAplicado hacerPregunta() {
		// El motor llama a hacerPregunta() en el MISMO turno en que arriesgarPersonaje() devolvió null,
		// así que reutilizamos la decisión del turno actual sin volver a avanzar el guion.
		Turno actual = (turnosJugados >= 1 && turnosJugados <= guion.size())
				? guion.get(turnosJugados - 1)
				: porDefecto;
		FiltroAplicado f = actual.preguntar();
		if (f != null) {
			preguntasHechas++;
		}
		return f;
	}
}
