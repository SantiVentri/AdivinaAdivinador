package tests;

import java.util.ArrayList;
import java.util.List;

import game.MotorJuego;
import model.FiltroAplicado;
import model.Personaje;
import model.Tablero;
import model.TipoFiltro;
import players.HistorialConsultas;
import players.Jugador;
import utils.PersonajeFactory;

public class MotorJuegoTest {

	public static void run() {
		List<Personaje> base = PersonajeFactory.crearPersonajes();
		Personaje secretoA = base.get(3);
		Personaje secretoB = base.get(7);

		T.section("MotorJuego - validaciones del constructor");

		StubJugador a0 = new StubJugador("A", new Tablero(base));
		StubJugador b0 = new StubJugador("B", new Tablero(base));
		a0.elegirPersonaje(secretoA);
		b0.elegirPersonaje(secretoB);

		T.expectThrows(IllegalArgumentException.class,
				() -> new MotorJuego(null, b0), "jugador1 null -> IllegalArgumentException");
		T.expectThrows(IllegalArgumentException.class,
				() -> new MotorJuego(a0, null), "jugador2 null -> IllegalArgumentException");
		T.expectThrows(IllegalArgumentException.class,
				() -> new MotorJuego(a0, b0, null), "historial null -> IllegalArgumentException");

		StubJugador sinSecreto = new StubJugador("SinSecreto", new Tablero(base));
		T.expectThrows(IllegalStateException.class,
				() -> new MotorJuego(sinSecreto, b0), "jugador sin personaje elegido -> IllegalStateException");

		T.section("MotorJuego - ganador cuando A arriesga correctamente");
		{
			StubJugador a = new StubJugador("A", new Tablero(base));
			StubJugador b = new StubJugador("B", new Tablero(base));
			a.elegirPersonaje(secretoA);
			b.elegirPersonaje(secretoB);
			// A arriesga el secreto de B en el turno 1
			a.programar(turnoArriesgar(secretoB));

			T.mute();
			MotorJuego motor = new MotorJuego(a, b, new HistorialConsultas());
			Jugador ganador = motor.jugar();
			T.unmute();

			T.check(ganador == a, "gana A (arriesgó el secreto de B)");
			T.check(motor.isPartidaTerminada(), "partidaTerminada = true");
			T.check(motor.getGanador() == a, "getGanador() == A");
		}

		T.section("MotorJuego - arriesgue equivocado saca el personaje del tablero propio de A");
		{
			StubJugador a = new StubJugador("A", new Tablero(base));
			StubJugador b = new StubJugador("B", new Tablero(base));
			a.elegirPersonaje(secretoA);
			b.elegirPersonaje(secretoB);

			Personaje equivocado = base.get(0); // no es el secreto de B
			int antes = a.getTablero().cantidadRestante();
			a.programar(turnoArriesgar(equivocado)); // turno 1 de A: falla
			b.programar(turnoArriesgar(secretoA)); // turno 1 de B: gana

			T.mute();
			MotorJuego motor = new MotorJuego(a, b, new HistorialConsultas());
			Jugador ganador = motor.jugar();
			T.unmute();

			T.eq(antes - 1, a.getTablero().cantidadRestante(), "el tablero de A quedó con 1 menos tras fallar");
			T.check(!a.getTablero().getPersonajesRestantes().contains(equivocado), "el personaje fallado se sacó del tablero de A");
			T.check(ganador == b, "después gana B");
		}

		T.section("MotorJuego - alternancia de turnos y hook alTerminarTurno");
		{
			StubJugador a = new StubJugador("A", new Tablero(base));
			StubJugador b = new StubJugador("B", new Tablero(base));
			a.elegirPersonaje(secretoA);
			b.elegirPersonaje(secretoB);

			// A pregunta 2 veces (turnos 1 y 2), después arriesga bien (turno 3)
			a.programar(turnoPreguntar(TipoFiltro.GENERO, "MASCULINO"),
					turnoPreguntar(TipoFiltro.EDAD, "ADULTO"),
					turnoArriesgar(secretoB));
			b.porDefecto(turnoPreguntar(TipoFiltro.LENTES, "true"));

			List<String> hook = new ArrayList<>();
			T.mute();
			MotorJuego motor = new MotorJuego(a, b, new HistorialConsultas(),
					jugadorQueJugo -> hook.add(jugadorQueJugo.getNombre()));
			Jugador ganador = motor.jugar();
			T.unmute();

			T.check(ganador == a, "gana A en su tercer turno");
			// A juega turnos 1,3,5; B juega 2,4. Antes de que A gane en el turno 5, el hook vio: A,B,A,B
			T.eq("[A, B, A, B]", hook.toString(), "el hook recibe al jugador que acaba de jugar, en orden, y NO se llama en el turno ganador");
		}

		T.section("MotorJuego - BUCLE INFINITO si ambos jugadores pasan siempre");
		{
			StubJugador a = new StubJugador("A", new Tablero(base)).topeDeTurnos(5000);
			StubJugador b = new StubJugador("B", new Tablero(base)).topeDeTurnos(5000);
			a.elegirPersonaje(secretoA);
			b.elegirPersonaje(secretoB);
			// ninguno arriesga ni pregunta nunca (porDefecto = null, null)

			T.mute();
			Throwable t = T.capture(() -> new MotorJuego(a, b, new HistorialConsultas()).jugar());
			T.unmute();

			if (t instanceof AssertionError) {
				T.finding("MOTOR-LOOP-1", "ALTA",
						"MotorJuego.jugar() no tiene tope de turnos. Si en un turno el jugador no arriesga y no tiene "
								+ "pregunta nueva, 'pasa el turno' y el while sigue. Con ambos jugadores pasando "
								+ "indefinidamente (o con un tablero que no se puede achicar más y sin querer arriesgar) "
								+ "queda en bucle infinito imprimiendo turnos. Con las máquinas reales hoy no se dispara "
								+ "(la aleatoria siempre 'pregunta' algo y la asertiva termina arriesgando), pero el motor "
								+ "no está blindado y cualquier Jugador que devuelva (null, null) lo cuelga.");
			} else if (t == null) {
				T.fail("se esperaba que el motor entrara en bucle (AssertionError del tope), pero terminó solo");
			} else {
				T.fail("el bucle produjo " + t.getClass().getName() + " en vez del tope esperado");
			}
		}

		T.section("MotorJuego - empate cuando ambos tableros quedan vacíos");
		{
			StubJugador a = new StubJugador("A", new Tablero(new ArrayList<>()));
			StubJugador b = new StubJugador("B", new Tablero(new ArrayList<>()));
			a.elegirPersonaje(secretoA); // el secreto no tiene por qué estar en el tablero
			b.elegirPersonaje(secretoB);

			T.mute();
			MotorJuego motor = new MotorJuego(a, b, new HistorialConsultas());
			Jugador ganador = motor.jugar();
			T.unmute();

			T.check(ganador == null, "empate -> jugar() devuelve null");
			if (!motor.isPartidaTerminada()) {
				T.finding("MOTOR-EMPATE-1", "BAJA",
						"En el empate (ambos tableros vacíos) MotorJuego hace break sin setear partidaTerminada. "
								+ "jugar() devuelve null pero isPartidaTerminada() sigue en false: un llamador que se guíe "
								+ "por isPartidaTerminada() en vez del valor de retorno cree que la partida sigue.");
			} else {
				T.pass("isPartidaTerminada() refleja el empate");
			}
		}

		T.section("MotorJuego - esPersonajeSecreto compara por identidad");
		{
			List<Personaje> otra = PersonajeFactory.crearPersonajes();
			StubJugador a = new StubJugador("A", new Tablero(base));
			StubJugador b = new StubJugador("B", new Tablero(base));
			a.elegirPersonaje(secretoA);
			b.elegirPersonaje(base.get(9));
			// A arriesga una instancia DISTINTA de otra lista, con los mismos datos que el secreto de B
			Personaje mismoDatoOtraInstancia = otra.get(9);
			a.programar(turnoArriesgar(mismoDatoOtraInstancia));
			b.programar(turnoArriesgar(secretoA)); // por si A no acierta, B cierra la partida

			T.mute();
			Jugador ganador = new MotorJuego(a, b, new HistorialConsultas()).jugar();
			T.unmute();

			if (ganador == b) {
				T.finding("MOTOR-EQ-1", "BAJA",
						"esPersonajeSecreto usa equals() y Personaje no lo implementa (identidad). Arriesgar 'el mismo "
								+ "personaje' pero como otra instancia se considera FALLO. Hoy no pasa porque el juego "
								+ "reusa una sola lista, pero es un supuesto implícito frágil.");
			} else if (ganador == a) {
				T.fail("dos instancias distintas con mismos datos se consideraron el mismo personaje (Personaje.equals por valor?)");
			}
		}
	}

	// ---- helpers de guion ----

	static StubJugador.Turno turnoArriesgar(Personaje p) {
		return new StubJugador.Turno() {
			public Personaje arriesgar() {
				return p;
			}

			public FiltroAplicado preguntar() {
				return null;
			}
		};
	}

	static StubJugador.Turno turnoPreguntar(TipoFiltro tipo, String valor) {
		return new StubJugador.Turno() {
			public Personaje arriesgar() {
				return null;
			}

			public FiltroAplicado preguntar() {
				return new FiltroAplicado(tipo, valor);
			}
		};
	}
}
