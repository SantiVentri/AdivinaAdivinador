package tests;

import java.util.ArrayList;
import java.util.List;

import model.Personaje;
import model.Tablero;
import model.TipoFiltro;
import utils.PersonajeFactory;

public class TableroTest {

	public static void run() {
		T.section("Tablero - filtrado básico");

		List<Personaje> todos = PersonajeFactory.crearPersonajes();
		Tablero t = new Tablero(todos);

		T.eq(23, t.cantidadRestante(), "arranca con 23");
		T.check(!t.estaVacio(), "no está vacío");
		T.check(!t.quedaUnoSolo(), "no queda uno solo");

		int masculinos = t.contarSiSeAplicara(TipoFiltro.GENERO, "MASCULINO");
		int femeninos = t.contarSiSeAplicara(TipoFiltro.GENERO, "FEMENINO");
		T.eq(23, masculinos + femeninos, "contarSiSeAplicara reparte todos por género");

		t.aplicarFiltro(TipoFiltro.GENERO, "MASCULINO", true);
		T.eq(masculinos, t.cantidadRestante(), "tras filtrar por 'es masculino' quedan solo los masculinos");
		for (Personaje p : t.getPersonajesRestantes()) {
			T.check(p.getGenero().name().equals("MASCULINO"), "restante " + p.getNombre() + " es masculino");
		}

		T.section("Tablero - filtrado con respuesta negativa");
		Tablero t2 = new Tablero(todos);
		int conLentes = t2.contarSiSeAplicara(TipoFiltro.LENTES, "true");
		t2.aplicarFiltro(TipoFiltro.LENTES, "true", false); // "NO usa lentes"
		T.eq(23 - conLentes, t2.cantidadRestante(), "tras 'no usa lentes' quedan 23 - (los que usan lentes)");

		T.section("Tablero - reiniciar");
		t2.reiniciar();
		T.eq(23, t2.cantidadRestante(), "reiniciar vuelve a 23");

		T.section("Tablero - sacarPersonaje");
		Tablero t3 = new Tablero(todos);
		Personaje victima = t3.getPersonajesRestantes().get(0);
		t3.sacarPersonaje(victima);
		T.eq(22, t3.cantidadRestante(), "sacar 1 -> 22");
		T.check(!t3.getPersonajesRestantes().contains(victima), "el sacado ya no está");
		t3.sacarPersonaje(victima); // de nuevo, ya no está
		T.eq(22, t3.cantidadRestante(), "sacar algo que no está no rompe ni cambia el conteo");

		// sacarPersonaje con una instancia distinta pero mismos datos: NO lo saca (Personaje no override equals)
		Tablero t4 = new Tablero(todos);
		List<Personaje> otraLista = PersonajeFactory.crearPersonajes();
		Personaje gemeloDistintaInstancia = otraLista.get(0); // mismo nombre/atributos, otra instancia
		int antes = t4.cantidadRestante();
		t4.sacarPersonaje(gemeloDistintaInstancia);
		if (t4.cantidadRestante() == antes) {
			T.pass("sacarPersonaje con otra instancia (mismos datos) NO saca nada -> identidad, no valor");
			T.finding("TAB-EQ-1", "BAJA",
					"Tablero.sacarPersonaje y Jugador.esPersonajeSecreto dependen de la identidad de instancia "
							+ "(Personaje no implementa equals/hashCode). Hoy funciona porque toda la partida comparte "
							+ "la misma lista de PersonajeFactory; si algo reconstruye personajes, arriesgar por valor deja de funcionar.");
		} else {
			T.fail("sacarPersonaje sacó un personaje de otra instancia: Personaje sí compara por valor?");
		}

		T.section("Tablero - buscarPorNombre");
		Tablero t5 = new Tablero(todos);
		T.check(t5.buscarPorNombre("harry potter") != null, "buscarPorNombre case-insensitive");
		T.check(t5.buscarPorNombre("  Harry Potter  ") != null, "buscarPorNombre hace trim");
		T.check(t5.buscarPorNombre("No Existe") == null, "buscarPorNombre inexistente -> null");
		T.expectThrows(NullPointerException.class, () -> t5.buscarPorNombre(null),
				"buscarPorNombre(null) lanza NPE (hace null.trim())");
		T.finding("TAB-NULL-1", "BAJA",
				"Tablero.buscarPorNombre(null) tira NullPointerException. Hoy no se llega con null desde el juego, "
						+ "pero no está blindado.");

		T.section("Tablero - lista inmutable");
		Tablero t6 = new Tablero(todos);
		T.expectThrows(UnsupportedOperationException.class,
				() -> t6.getPersonajesRestantes().add(null),
				"getPersonajesRestantes() devuelve lista inmutable");

		T.section("Tablero - construido con lista vacía");
		Tablero vacio = new Tablero(new ArrayList<>());
		T.check(vacio.estaVacio(), "tablero vacío estaVacio()");
		T.check(!vacio.quedaUnoSolo(), "tablero vacío no 'queda uno solo'");
		T.expectNoThrow(() -> vacio.aplicarFiltro(TipoFiltro.GENERO, "MASCULINO", true), "filtrar tablero vacío no rompe");
		T.eq(0, vacio.contarSiSeAplicara(TipoFiltro.GENERO, "MASCULINO"), "contar en vacío -> 0");

		T.section("Tablero - el constructor NO copia en profundidad (comparte instancias)");
		List<Personaje> fuente = PersonajeFactory.crearPersonajes();
		Tablero tA = new Tablero(fuente);
		Tablero tB = new Tablero(fuente);
		T.check(tA.getPersonajesRestantes().get(0) == tB.getPersonajesRestantes().get(0),
				"dos tableros de la misma fuente comparten las instancias de Personaje (esperado por diseño)");
	}
}
