package tests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Genero;
import model.Personaje;
import utils.PersonajeFactory;

public class PersonajeFactoryTest {

	public static void run() {
		T.section("PersonajeFactory - cantidad y unicidad");

		List<Personaje> ps = PersonajeFactory.crearPersonajes();
		T.eq(23, ps.size(), "crea exactamente 23 personajes");

		Set<String> nombres = new HashSet<>();
		for (Personaje p : ps) {
			nombres.add(p.getNombre());
		}
		T.eq(23, nombres.size(), "los 23 nombres son únicos");

		T.section("PersonajeFactory - orden por género + id autoincremental");

		boolean vioFemenino = false;
		boolean ordenOk = true;
		for (int i = 0; i < ps.size(); i++) {
			Personaje p = ps.get(i);
			if (p.getId() != i + 1) {
				ordenOk = false;
			}
			if (p.getGenero() == Genero.FEMENINO) {
				vioFemenino = true;
			} else if (vioFemenino && p.getGenero() == Genero.MASCULINO) {
				T.fail("hay un MASCULINO (" + p.getNombre() + ") después de un FEMENINO: no está ordenado solo por género");
			}
		}
		T.check(ordenOk, "los id van 1..23 correlativos, en el orden de la lista");
		T.check(ps.get(0).getGenero() == Genero.MASCULINO, "arranca por MASCULINO (Genero.MASCULINO.ordinal()=0)");

		T.section("PersonajeFactory - cada llamada numera desde 1 (sin contador static)");
		List<Personaje> ps2 = PersonajeFactory.crearPersonajes();
		T.eq(1, ps2.get(0).getId(), "segunda llamada: primer id vuelve a ser 1");
		T.eq(23, ps2.get(22).getId(), "segunda llamada: último id es 23");
		T.check(ps.get(0) != ps2.get(0), "cada llamada crea instancias nuevas");

		T.section("PersonajeFactory - hay dos personajes indistinguibles");
		// Fred y George Weasley tienen los 8 atributos idénticos.
		Personaje fred = find(ps, "Fred Weasley");
		Personaje george = find(ps, "George Weasley");
		boolean iguales = fred.getGenero() == george.getGenero()
				&& fred.getEdad() == george.getEdad()
				&& fred.getColorPelo() == george.getColorPelo()
				&& fred.esCalvo() == george.esCalvo()
				&& fred.tieneLentes() == george.tieneLentes()
				&& fred.getCasa() == george.getCasa()
				&& fred.getSangreLimpia() == george.getSangreLimpia()
				&& fred.esAlumno() == george.esAlumno();
		if (iguales) {
			T.finding("FACT-TWINS-1", "MEDIA",
					"Fred Weasley y George Weasley tienen los 8 atributos idénticos: NINGUNA combinación de preguntas "
							+ "los separa. Si el personaje secreto es uno de ellos, el rival siempre queda con 2 candidatos "
							+ "y tiene que adivinar a ciegas (50%). No rompe el juego pero contradice el espíritu de "
							+ "'características distinguibles' de la consigna.");
		} else {
			T.pass("Fred y George se distinguen por algún atributo");
		}
	}

	static Personaje find(List<Personaje> ps, String nombre) {
		for (Personaje p : ps) {
			if (p.getNombre().equals(nombre)) {
				return p;
			}
		}
		throw new IllegalStateException("no está " + nombre);
	}
}
