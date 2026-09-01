package tests;

import java.util.List;

import model.FiltroAplicado;
import model.TipoFiltro;
import players.Consulta;
import players.HistorialConsultas;

public class FiltroYHistorialTest {

	public static void run() {
		T.section("FiltroAplicado.clave");

		FiltroAplicado f = new FiltroAplicado(TipoFiltro.COLOR_PELO, "negro");
		T.eq("COLOR_PELO=negro", f.clave(), "clave = TIPO=valor");
		T.eq("COLOR_PELO=negro", FiltroAplicado.clave(TipoFiltro.COLOR_PELO, "negro"), "clave estática coincide");
		T.eq(f.clave(), f.toString(), "toString == clave");

		// La clave NO normaliza mayúsculas: humano manda 'negro', máquina manda 'NEGRO' -> claves distintas como string
		FiltroAplicado fHumano = new FiltroAplicado(TipoFiltro.COLOR_PELO, "negro");
		FiltroAplicado fMaquina = new FiltroAplicado(TipoFiltro.COLOR_PELO, "NEGRO");
		T.check(!fHumano.clave().equals(fMaquina.clave()),
				"clave('negro') != clave('NEGRO') como String");
		T.finding("FILT-CLAVE-1", "BAJA",
				"FiltroAplicado.clave no normaliza el valor. El humano genera 'COLOR_PELO=negro' y las máquinas "
						+ "'COLOR_PELO=NEGRO'. El historial igual las trata como iguales porque compara con equalsIgnoreCase, "
						+ "pero cualquier comparación de claves por igualdad de String directa fallaría.");

		FiltroAplicado fNull = new FiltroAplicado(TipoFiltro.GENERO, null);
		T.eq("GENERO=null", fNull.clave(), "clave con valor null -> 'GENERO=null' (no rompe)");

		T.section("HistorialConsultas - yaFuePreguntado por jugador");

		HistorialConsultas h = new HistorialConsultas();
		h.agregarConsulta("Ana", new FiltroAplicado(TipoFiltro.GENERO, "MASCULINO"), true);
		h.agregarConsulta("Beto", new FiltroAplicado(TipoFiltro.LENTES, "true"), false);

		T.check(h.yaFuePreguntado("Ana", "GENERO=MASCULINO"), "Ana ya preguntó GENERO=MASCULINO");
		T.check(h.yaFuePreguntado("ana", "genero=masculino"), "yaFuePreguntado es case-insensitive en nombre y pregunta");
		T.check(!h.yaFuePreguntado("Beto", "GENERO=MASCULINO"), "Beto NO preguntó eso (aislado por jugador)");
		T.check(!h.yaFuePreguntado("Ana", "LENTES=true"), "Ana NO preguntó LENTES");
		T.eq(2, h.getCantidadConsultas(), "hay 2 consultas");

		T.expectThrows(NullPointerException.class,
				() -> h.yaFuePreguntado("Ana", null),
				"yaFuePreguntado(nombre, null) lanza NPE (hace null.trim())");
		T.finding("HIST-NULL-1", "BAJA",
				"HistorialConsultas.yaFuePreguntado(nombre, null) tira NPE. No se llega desde el juego pero no está blindado.");

		T.section("HistorialConsultas - obtenerConsultasDe");
		List<Consulta> deAna = h.obtenerConsultasDe("ANA");
		T.eq(1, deAna.size(), "obtenerConsultasDe('ANA') -> 1 (case-insensitive)");
		T.expectThrows(UnsupportedOperationException.class, () -> deAna.add(null), "la lista devuelta es inmutable");
		T.eq(0, h.obtenerConsultasDe("Nadie").size(), "obtenerConsultasDe de alguien sin consultas -> lista vacía");

		T.section("HistorialConsultas - reiniciar y consultas duplicadas");
		h.agregarConsulta("Ana", new FiltroAplicado(TipoFiltro.GENERO, "MASCULINO"), true); // duplicada exacta
		T.eq(3, h.getCantidadConsultas(), "agregar la misma consulta de nuevo NO deduplica (lo hace quien llama)");
		h.reiniciar();
		T.eq(0, h.getCantidadConsultas(), "reiniciar deja el historial en 0");

		T.section("HistorialConsultas - mismo nombre, jugadores distintos");
		HistorialConsultas h2 = new HistorialConsultas();
		h2.agregarConsulta("Máquina Asertiva", new FiltroAplicado(TipoFiltro.EDAD, "ADULTO"), true);
		h2.agregarConsulta("Máquina Aleatoria", new FiltroAplicado(TipoFiltro.EDAD, "ADULTO"), true);
		T.check(h2.yaFuePreguntado("Máquina Asertiva", "EDAD=ADULTO"), "asertiva ve la suya");
		T.check(h2.yaFuePreguntado("Máquina Aleatoria", "EDAD=ADULTO"), "aleatoria ve la suya");
		T.finding("HIST-SHARE-1", "BAJA",
				"En Máquina vs Máquina las dos máquinas comparten el HistorialConsultas pero cada una solo consulta "
						+ "por su propio nombre. La 'ventaja' de la máquina 2 de conocer las preguntas de la máquina 1 "
						+ "NO se usa en el modo espectador (decisión de diseño, no crash).");
	}
}
