package tests;

import model.CasaHogwarts;
import model.ColorPelo;
import model.Edad;
import model.Genero;
import model.Personaje;
import model.SangreLimpia;
import model.TipoFiltro;

public class PersonajeTest {

	static Personaje harry() {
		// Harry: masculino, adolescente, pelo negro, no calvo, con lentes, Gryffindor, mestizo, alumno
		return new Personaje("Harry Potter", Genero.MASCULINO, Edad.ADOLESCENTE, ColorPelo.NEGRO,
				false, true, CasaHogwarts.GRYFFINDOR, SangreLimpia.MESTIZO, true);
	}

	public static void run() {
		T.section("Personaje.cumpleFiltro");

		Personaje h = harry();

		T.check(h.cumpleFiltro(TipoFiltro.GENERO, "MASCULINO"), "genero MASCULINO");
		T.check(h.cumpleFiltro(TipoFiltro.GENERO, "masculino"), "genero case-insensitive");
		T.check(!h.cumpleFiltro(TipoFiltro.GENERO, "FEMENINO"), "genero FEMENINO -> false");
		T.check(h.cumpleFiltro(TipoFiltro.EDAD, "adolescente"), "edad adolescente");
		T.check(h.cumpleFiltro(TipoFiltro.COLOR_PELO, "negro"), "pelo negro");
		T.check(h.cumpleFiltro(TipoFiltro.CASA_HOGWARTS, "gryffindor"), "casa gryffindor");
		T.check(h.cumpleFiltro(TipoFiltro.SANGRE_LIMPIA, "mestizo"), "sangre mestizo");
		T.check(h.cumpleFiltro(TipoFiltro.LENTES, "true"), "lentes true");
		T.check(!h.cumpleFiltro(TipoFiltro.CALVICIE, "true"), "calvicie true -> false");
		T.check(h.cumpleFiltro(TipoFiltro.CALVICIE, "false"), "calvicie false -> true");
		T.check(h.cumpleFiltro(TipoFiltro.ALUMNO, "true"), "alumno true");

		// Booleanos con basura: Boolean.parseBoolean("cualquier cosa") == false
		T.check(h.cumpleFiltro(TipoFiltro.LENTES, "TRUE"), "lentes 'TRUE' (parseBoolean es case-insensitive)");
		T.check(!h.cumpleFiltro(TipoFiltro.LENTES, "sí"), "lentes 'sí' -> parseBoolean=false");
		T.check(!h.cumpleFiltro(TipoFiltro.LENTES, "1"), "lentes '1' -> parseBoolean=false");
		T.finding("PERS-BOOL-1", "BAJA",
				"cumpleFiltro para CALVICIE/LENTES/ALUMNO usa Boolean.parseBoolean: cualquier string que no sea "
						+ "'true' cuenta como false silenciosamente. Un valor mal tipeado no da error, solo respuesta incorrecta.");

		// Enum inexistente: no matchea, no explota
		T.expectNoThrow(() -> h.cumpleFiltro(TipoFiltro.COLOR_PELO, "turquesa"), "color inexistente no lanza");
		T.check(!h.cumpleFiltro(TipoFiltro.COLOR_PELO, "turquesa"), "color inexistente -> false");

		// null como valor esperado
		Throwable tnull = T.capture(() -> h.cumpleFiltro(TipoFiltro.GENERO, null));
		T.check(tnull == null && !h.cumpleFiltro(TipoFiltro.GENERO, null),
				"cumpleFiltro(GENERO, null) no lanza y da false (equalsIgnoreCase(null)=false)");
		Throwable tnullBool = T.capture(() -> h.cumpleFiltro(TipoFiltro.CALVICIE, null));
		if (tnullBool == null) {
			T.pass("cumpleFiltro(CALVICIE, null) no lanza (parseBoolean(null)=false)");
		} else {
			T.fail("cumpleFiltro(CALVICIE, null) lanzó " + tnullBool.getClass().getSimpleName());
		}

		T.section("Personaje.asignarOrden");

		Personaje p = harry();
		T.eq(-1, p.getId(), "id arranca en -1 (sin asignar)");
		T.expectNoThrow(() -> p.asignarOrden(1), "primer asignarOrden ok");
		T.eq(1, p.getId(), "id quedó en 1");
		T.expectThrows(IllegalStateException.class, () -> p.asignarOrden(2), "segundo asignarOrden lanza IllegalStateException");
		T.eq(1, p.getId(), "id sigue en 1 tras el intento fallido");

		// asignarOrden acepta cualquier int, incluso negativos o 0 (no valida el rango)
		Personaje p2 = harry();
		T.expectNoThrow(() -> p2.asignarOrden(-5), "asignarOrden(-5) no valida y lo acepta");
		T.eq(-5, p2.getId(), "id = -5");
		T.finding("PERS-ORD-1", "BAJA",
				"asignarOrden acepta 0 y negativos. Si por error se llama con 0 el personaje queda 'con orden' "
						+ "pero con un valor sin sentido; con -1 vuelve a considerarse 'sin asignar' y se podría re-asignar.");
		Personaje p3 = harry();
		p3.asignarOrden(-1); // vuelve a dejarlo como 'sin asignar'
		T.expectNoThrow(() -> p3.asignarOrden(9), "tras asignarOrden(-1) se puede re-asignar (el guardián usa -1 como centinela)");

		T.section("Personaje.toString");
		T.expectNoThrow(() -> harry().toString(), "toString no lanza");
		T.check(harry().toString().contains("Harry Potter"), "toString incluye el nombre");
	}
}
