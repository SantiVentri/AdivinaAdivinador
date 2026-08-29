package utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import model.CasaHogwarts;
import model.ColorPelo;
import model.Edad;
import model.Genero;
import model.Personaje;
import model.SangreLimpia;

public class PersonajeFactory {
	public static List<Personaje> crearPersonajes() {
		List<Personaje> personajes = new ArrayList<Personaje>();

		personajes.add(new Personaje("Harry Potter", Genero.MASCULINO, Edad.ADOLESCENTE,
				ColorPelo.NEGRO, false, true, CasaHogwarts.GRYFFINDOR, SangreLimpia.MESTIZO, true));

		personajes.add(new Personaje("Ron Weasley", Genero.MASCULINO, Edad.ADOLESCENTE,
				ColorPelo.COLORADO, false, false, CasaHogwarts.GRYFFINDOR, SangreLimpia.MAGO, true));

		personajes.add(new Personaje("Hermione Granger", Genero.FEMENINO, Edad.ADOLESCENTE,
				ColorPelo.MARRON, false, false, CasaHogwarts.GRYFFINDOR, SangreLimpia.MUGGLE, true));

		personajes.add(new Personaje("Fred Weasley", Genero.MASCULINO, Edad.ADOLESCENTE,
				ColorPelo.COLORADO, false, false, CasaHogwarts.GRYFFINDOR, SangreLimpia.MAGO, true));

		personajes.add(new Personaje("George Weasley", Genero.MASCULINO, Edad.ADOLESCENTE,
				ColorPelo.COLORADO, false, false, CasaHogwarts.GRYFFINDOR, SangreLimpia.MAGO, true));

		personajes.add(new Personaje("Ginny Weasley", Genero.FEMENINO, Edad.ADOLESCENTE,
				ColorPelo.COLORADO, false, false, CasaHogwarts.GRYFFINDOR, SangreLimpia.MAGO, true));

		personajes.add(new Personaje("Albus Dumbledore", Genero.MASCULINO, Edad.ANCIANO,
				ColorPelo.BLANCO, false, true, CasaHogwarts.GRYFFINDOR, SangreLimpia.MESTIZO, false));

		personajes.add(new Personaje("Minerva McGonagall", Genero.FEMENINO, Edad.ANCIANO,
				ColorPelo.NEGRO, false, true, CasaHogwarts.GRYFFINDOR, SangreLimpia.MESTIZO, false));

		personajes.add(new Personaje("Neville Longbottom", Genero.MASCULINO, Edad.ADOLESCENTE,
				ColorPelo.MARRON, false, false, CasaHogwarts.GRYFFINDOR, SangreLimpia.MAGO, true));

		personajes.add(new Personaje("Remus Lupin", Genero.MASCULINO, Edad.ADULTO,
				ColorPelo.MARRON, false, false, CasaHogwarts.GRYFFINDOR, SangreLimpia.MESTIZO, false));

		personajes.add(new Personaje("Sirius Black", Genero.MASCULINO, Edad.ADULTO,
				ColorPelo.NEGRO, false, false, CasaHogwarts.GRYFFINDOR, SangreLimpia.MAGO, false));

		personajes.add(new Personaje("Draco Malfoy", Genero.MASCULINO, Edad.ADOLESCENTE,
				ColorPelo.AMARILLO, false, false, CasaHogwarts.SLYTHERIN, SangreLimpia.MAGO, true));

		personajes.add(new Personaje("Severus Snape", Genero.MASCULINO, Edad.ADULTO,
				ColorPelo.NEGRO, false, false, CasaHogwarts.SLYTHERIN, SangreLimpia.MESTIZO, false));

		personajes.add(new Personaje("Lord Voldemort", Genero.MASCULINO, Edad.ANCIANO,
				ColorPelo.NEGRO, true, false, CasaHogwarts.SLYTHERIN, SangreLimpia.MESTIZO, false));

		personajes.add(new Personaje("Bellatrix Lestrange", Genero.FEMENINO, Edad.ADULTO,
				ColorPelo.NEGRO, false, false, CasaHogwarts.SLYTHERIN, SangreLimpia.MAGO, false));

		personajes.add(new Personaje("Cedric Diggory", Genero.MASCULINO, Edad.ADOLESCENTE,
				ColorPelo.MARRON, false, false, CasaHogwarts.HUFFLEPUFF, SangreLimpia.MESTIZO, true));

		personajes.add(new Personaje("Nymphadora Tonks", Genero.FEMENINO, Edad.ADULTO,
				ColorPelo.ROSA, false, false, CasaHogwarts.HUFFLEPUFF, SangreLimpia.MESTIZO, false));

		personajes.add(new Personaje("Newt Scamander", Genero.MASCULINO, Edad.ADULTO,
				ColorPelo.AMARILLO, false, false, CasaHogwarts.HUFFLEPUFF, SangreLimpia.MESTIZO, false));

		personajes.add(new Personaje("Pomona Sprout", Genero.FEMENINO, Edad.ANCIANO,
				ColorPelo.GRIS, false, false, CasaHogwarts.HUFFLEPUFF, SangreLimpia.MESTIZO, false));

		personajes.add(new Personaje("Cho Chang", Genero.FEMENINO, Edad.ADOLESCENTE,
				ColorPelo.NEGRO, false, false, CasaHogwarts.RAVENCLAW, SangreLimpia.MESTIZO, true));

		personajes.add(new Personaje("Luna Lovegood", Genero.FEMENINO, Edad.ADOLESCENTE,
				ColorPelo.AMARILLO, false, false, CasaHogwarts.RAVENCLAW, SangreLimpia.MAGO, true));

		personajes.add(new Personaje("Filius Flitwick", Genero.MASCULINO, Edad.ANCIANO,
				ColorPelo.BLANCO, false, false, CasaHogwarts.RAVENCLAW, SangreLimpia.MESTIZO, false));

		personajes.add(new Personaje("Gilderoy Lockhart", Genero.MASCULINO, Edad.ADULTO,
				ColorPelo.AMARILLO, false, false, CasaHogwarts.RAVENCLAW, SangreLimpia.MESTIZO, false));
		
		ordenarPorGenero(personajes);
		
		return personajes;
	}
	
	private static void ordenarPorGenero(List<Personaje> personajes) {
		personajes.sort(Comparator.comparing(Personaje::getGenero));
	}
}