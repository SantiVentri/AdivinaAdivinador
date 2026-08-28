package model;

public class Personaje {
	private static int contador = 0; 
	private int id;
	private String nombre;
	
	// Atributos físicos
	private Genero genero;
	private Edad edad;
	private ColorPiel colorPiel;
	private ColorOjos colorOjos;
	private ColorPelo colorPelo;
	private boolean calvicie;
	
	// Vestimenta
	private boolean lentes;
	private boolean sombrero;
	
	// Constructor
	public Personaje(String nombre, Genero genero, Edad edad, ColorPiel colorPiel, ColorOjos colorOjos,
			ColorPelo colorPelo, boolean calvicie, boolean lentes, boolean sombrero) {
		this.id = contador++;
		this.nombre = nombre;
		this.genero = genero;
		this.edad = edad;
		this.colorPiel = colorPiel;
		this.colorOjos = colorOjos;
		this.colorPelo = colorPelo;
		this.calvicie = calvicie;
		this.lentes = lentes;
		this.sombrero = sombrero;
	}
	
	// Métodos
	public boolean cumpleFiltro(TipoFiltro tipo, String valorEsperado) {
		switch (tipo) {
	        case GENERO:
	            return genero.name().equalsIgnoreCase(valorEsperado);
	        case EDAD:
	        	return edad.name().equalsIgnoreCase(valorEsperado);
	        case COLOR_PIEL:
	            return colorPiel.name().equalsIgnoreCase(valorEsperado);
	        case COLOR_OJOS:
	            return colorOjos.name().equalsIgnoreCase(valorEsperado);
	        case COLOR_PELO:
	            return colorPelo.name().equalsIgnoreCase(valorEsperado);
	        case CALVICIE:
	            return calvicie == Boolean.parseBoolean(valorEsperado);
	        case LENTES:
	            return lentes == Boolean.parseBoolean(valorEsperado);
	        case SOMBRERO:
	            return sombrero == Boolean.parseBoolean(valorEsperado);
	        default:
	            return false;
		}
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
	
	public Edad getEdad() {
		return edad;
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
	    return String.format("[%02d] %-15s | %-9s | %-10s | Calvo: %-3s | Lentes: %-3s | Pelo: %s",
	            id, nombre, genero, edad, (calvicie ? "Sí" : "No"), (lentes ? "Sí" : "No"), colorPelo);
	}
}
