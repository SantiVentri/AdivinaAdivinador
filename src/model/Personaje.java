package model;

public class Personaje {
	private static int contador = 0; 
	private int id;
	private String nombre;
	
	// Atributos físicos
	private Genero genero;
	private ColorPiel colorPiel;
	private ColorOjos colorOjos;
	private ColorPelo colorPelo;
	private boolean calvicie;
	
	// Vestimenta
	private boolean lentes;
	private boolean sombrero;
	
	// Constructor
	public Personaje(String nombre, Genero genero, ColorPiel colorPiel, ColorOjos colorOjos,
			ColorPelo colorPelo, boolean calvicie, boolean lentes, boolean sombrero) {
		this.id = contador++;
		this.nombre = nombre;
		this.genero = genero;
		this.colorPiel = colorPiel;
		this.colorOjos = colorOjos;
		this.colorPelo = colorPelo;
		this.calvicie = calvicie;
		this.lentes = lentes;
		this.sombrero = sombrero;
	}
	
	// Getters
	public int getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public Genero getGenero() {
		return genero;
	}

	public ColorPiel getColorPiel() {
		return colorPiel;
	}

	public ColorOjos getColorOjos() {
		return colorOjos;
	}

	public ColorPelo getColorPelo() {
		return colorPelo;
	}

	public boolean esCalvo() {
		return calvicie;
	}

	public boolean tieneLentes() {
		return lentes;
	}

	public boolean tieneSombrero() {
		return sombrero;
	}
	
	@Override
	public String toString() {
	    return String.format("[%02d] %-15s | %-9s | Calvo: %-3s | Lentes: %-3s | Pelo: %s",
	            id, nombre, genero, (calvicie ? "Sí" : "No"), (lentes ? "Sí" : "No"), colorPelo);
	}
}
