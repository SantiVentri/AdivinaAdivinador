package tests;

import java.util.List;

import model.CasaHogwarts;
import model.ColorPelo;
import model.Edad;
import model.FiltroAplicado;
import model.Genero;
import model.Personaje;
import model.SangreLimpia;
import model.Tablero;
import model.TipoFiltro;
import players.HistorialConsultas;
import players.Jugador;
import players.MaquinaAleatoria;
import players.MaquinaAsertiva;
import utils.PersonajeFactory;

public class MaquinasTest {

	public static void run() {
		List<Personaje> base = PersonajeFactory.crearPersonajes();

		T.section("Maquina - responderPregunta sin secreto elegido");
		{
			MaquinaAsertiva m = new MaquinaAsertiva(new Tablero(base), new HistorialConsultas());
			T.expectThrows(IllegalStateException.class,
					() -> m.responderPregunta(new FiltroAplicado(TipoFiltro.GENERO, "MASCULINO")),
					"responderPregunta antes de elegirPersonaje -> IllegalStateException");
			T.expectThrows(IllegalStateException.class,
					() -> m.esPersonajeSecreto(base.get(0)),
					"esPersonajeSecreto antes de elegirPersonaje -> IllegalStateException");
		}

		T.section("MaquinaAsertiva - elige la división más equilibrada");
		{
			HistorialConsultas h = new HistorialConsultas();
			MaquinaAsertiva m = new MaquinaAsertiva(new Tablero(base), h);
			m.elegirPersonaje(base.get(0));

			T.mute();
			FiltroAplicado elegido = m.hacerPregunta();
			T.unmute();

			int count = new Tablero(base).contarSiSeAplicara(elegido.getTipo(), elegido.getValor());
			int mejorPosible = mejorConteoPosible(new Tablero(base), 23);
			double difElegido = Math.abs(count - 11.5);
			double difMejor = Math.abs(mejorPosible - 11.5);
			T.check(difElegido <= difMejor + 0.001,
					"el filtro elegido (" + elegido.clave() + ", parte " + count + "/23) es tan bueno como el mejor posible ("
							+ mejorPosible + "/23)");
		}

		T.section("MaquinaAsertiva - no repite preguntas");
		{
			HistorialConsultas h = new HistorialConsultas();
			MaquinaAsertiva m = new MaquinaAsertiva(new Tablero(base), h);
			m.elegirPersonaje(base.get(0));

			java.util.Set<String> vistas = new java.util.HashSet<>();
			boolean repitio = false;
			T.mute();
			for (int i = 0; i < 8; i++) {
				FiltroAplicado f = m.hacerPregunta();
				if (f == null) {
					break;
				}
				if (!vistas.add(f.clave().toUpperCase())) {
					repitio = true;
				}
				// simular que MotorJuego la registra
				h.agregarConsulta(m.getNombre(), f, true);
			}
			T.unmute();
			T.check(!repitio, "8 preguntas seguidas, ninguna repetida (dedup por historial)");
		}

		T.section("MaquinaAsertiva - no arriesga con muchas opciones, sí con una sola");
		{
			HistorialConsultas h = new HistorialConsultas();
			MaquinaAsertiva m = new MaquinaAsertiva(new Tablero(base), h);
			m.elegirPersonaje(base.get(0));
			T.mute();
			Personaje temprano = m.arriesgarPersonaje();
			T.unmute();
			T.check(temprano == null, "con 23 opciones y preguntas disponibles, no arriesga");

			// dejar el tablero en 1
			Personaje target = base.get(5);
			Tablero t1 = new Tablero(base);
			for (TipoFiltro tipo : TipoFiltro.values()) {
				for (String v : valores(tipo)) {
					t1.aplicarFiltro(tipo, v, target.cumpleFiltro(tipo, v));
				}
			}
			MaquinaAsertiva m2 = new MaquinaAsertiva(t1, new HistorialConsultas());
			m2.elegirPersonaje(base.get(0));
			T.mute();
			Personaje forzado = m2.arriesgarPersonaje();
			T.unmute();
			T.check(forzado != null, "con el tablero reducido a 1, arriesga");
		}

		T.section("MaquinaAleatoria - hacerPregunta nunca devuelve null");
		{
			HistorialConsultas h = new HistorialConsultas();
			MaquinaAleatoria m = new MaquinaAleatoria(new Tablero(base), h);
			m.elegirPersonaje(base.get(0));
			boolean algunNull = false;
			T.mute();
			for (int i = 0; i < 200; i++) {
				FiltroAplicado f = m.hacerPregunta();
				if (f == null) {
					algunNull = true;
				}
				h.agregarConsulta(m.getNombre(), f, true);
			}
			T.unmute();
			T.check(!algunNull, "200 llamadas a hacerPregunta, nunca null (aunque ya no queden preguntas nuevas repite alguna)");
		}

		T.section("MaquinaAleatoria - arriesga forzado cuando queda una sola opción");
		{
			Personaje target = base.get(9);
			Tablero t1 = new Tablero(base);
			for (TipoFiltro tipo : TipoFiltro.values()) {
				for (String v : valores(tipo)) {
					t1.aplicarFiltro(tipo, v, target.cumpleFiltro(tipo, v));
				}
			}
			// t1 puede tener 1 (o 2 si cae en Fred/George). Forzamos a 1.
			while (t1.cantidadRestante() > 1) {
				t1.sacarPersonaje(t1.getPersonajesRestantes().get(t1.cantidadRestante() - 1));
			}
			MaquinaAleatoria m = new MaquinaAleatoria(t1, new HistorialConsultas());
			m.elegirPersonaje(base.get(0));
			T.mute();
			Personaje g = m.arriesgarPersonaje();
			T.unmute();
			T.check(g != null, "con 1 sola opción, la aleatoria arriesga sí o sí");
		}

		T.section("Maquina - drive en solitario: siempre termina y acierta");
		{
			int maxPasos = 0;
			for (int idx = 0; idx < base.size(); idx++) {
				Personaje target = base.get(idx);

				HistorialConsultas hA = new HistorialConsultas();
				MaquinaAsertiva a = new MaquinaAsertiva(new Tablero(base), hA);
				int pasosA = driveSolo(a, hA, target, "Máquina Asertiva");

				HistorialConsultas hB = new HistorialConsultas();
				MaquinaAleatoria b = new MaquinaAleatoria(new Tablero(base), hB);
				int pasosB = driveSolo(b, hB, target, "Máquina Aleatoria");

				maxPasos = Math.max(maxPasos, Math.max(pasosA, pasosB));
			}
			T.realOut().println("    (peor caso de pasos para adivinar cualquier personaje: " + maxPasos + ")");
			T.check(maxPasos > 0 && maxPasos < 500,
					"ninguna máquina se cuelga adivinando en solitario (peor caso " + maxPasos + " pasos)");
		}
	}

	/**
	 * Simula una máquina jugando sola contra un personaje fijo (respuestas calculadas del target),
	 * replicando el bucle de MotorJuego: arriesgar -> si null, preguntar y registrar en el historial.
	 * Devuelve la cantidad de pasos hasta que arriesga bien.
	 */
	static int driveSolo(Jugador maquina, HistorialConsultas h, Personaje target, String nombre) {
		maquina.elegirPersonaje(target); // su propio secreto es irrelevante para su búsqueda
		int pasos = 0;
		T.mute();
		try {
			while (pasos < 1000) {
				pasos++;
				Personaje g = maquina.arriesgarPersonaje();
				if (g != null) {
					if (g == target) {
						return pasos;
					}
					maquina.getTablero().sacarPersonaje(g); // arriesgó mal: MotorJuego lo saca del tablero
					continue;
				}
				FiltroAplicado f = maquina.hacerPregunta();
				if (f == null) {
					T.unmute();
					T.fail("(" + nombre + " vs " + target.getNombre() + ") atascada: ni arriesga ni pregunta");
					return pasos;
				}
				boolean resp = target.cumpleFiltro(f.getTipo(), f.getValor());
				h.agregarConsulta(maquina.getNombre(), f, resp); // como hace MotorJuego
				maquina.filtrarOpciones(f, resp);
			}
			T.unmute();
			T.fail("(" + nombre + " vs " + target.getNombre() + ") no terminó en 1000 pasos");
			return pasos;
		} finally {
			T.unmute();
		}
	}

	// ---- helpers ----

	static String[] valores(TipoFiltro tipo) {
		switch (tipo) {
			case GENERO:
				return names(Genero.values());
			case EDAD:
				return names(Edad.values());
			case COLOR_PELO:
				return names(ColorPelo.values());
			case CASA_HOGWARTS:
				return names(CasaHogwarts.values());
			case SANGRE_LIMPIA:
				return names(SangreLimpia.values());
			default:
				return new String[] { "true", "false" };
		}
	}

	static String[] names(Enum<?>[] vs) {
		String[] out = new String[vs.length];
		for (int i = 0; i < vs.length; i++) {
			out[i] = vs[i].name();
		}
		return out;
	}

	static int mejorConteoPosible(Tablero t, int total) {
		double half = total / 2.0;
		int mejor = 0;
		double mejorDif = Double.MAX_VALUE;
		for (TipoFiltro tipo : TipoFiltro.values()) {
			for (String v : valores(tipo)) {
				int c = t.contarSiSeAplicara(tipo, v);
				double d = Math.abs(c - half);
				if (d < mejorDif) {
					mejorDif = d;
					mejor = c;
				}
			}
		}
		return mejor;
	}
}
