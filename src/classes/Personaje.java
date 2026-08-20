package classes;

import types.COLORES_OJOS;
import types.COLORES_PELO;
import types.COLORES_PIEL;
import types.GENEROS;

public class Personaje {
	String nombre;
	GENEROS genero;
	boolean esPelado;
	boolean tieneLentes;
	COLORES_PELO colorPelo;
	COLORES_OJOS colorOjos;
	COLORES_PIEL colorPiel;

	public Personaje(String nombre, GENEROS genero, boolean esPelado, boolean tieneLentes, COLORES_PELO colorPelo,
			COLORES_OJOS colorOjos, COLORES_PIEL colorPiel) {
		this.nombre = nombre;
		this.genero = genero;
		this.esPelado = esPelado;
		this.tieneLentes = tieneLentes;
		this.colorPelo = colorPelo;
		this.colorOjos = colorOjos;
		this.colorPiel = colorPiel;
	}
	
	public String getNombre() {
		return nombre;
	}

	public GENEROS getGenero() {
		return genero;
	}

	public boolean esPelado() {
		return esPelado;
	}

	public boolean tieneLentes() {
		return tieneLentes;
	}

	public COLORES_PELO getColorPelo() {
		return colorPelo;
	}

	public COLORES_OJOS getColorOjos() {
		return colorOjos;
	}

	public COLORES_PIEL getColorPiel() {
		return colorPiel;
	}
}
