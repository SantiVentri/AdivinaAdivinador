package model;

public class Personaje {
	private static int contador = 0; 
	private int id;
	private String nombre;
	
	// Atributos generales
	private Genero genero;
	private Edad edad;
	private ColorOjos colorOjos;
	private ColorPelo colorPelo;
	private boolean calvicie;
	private boolean lentes;
	
	// Atributos de personaje de Harry Potter
	private CasaHogwarts casa;
	private SangreLimpia sangreLimpia;
	private boolean alumno;
	
	// Constructor
	public Personaje(String nombre, Genero genero, Edad edad, ColorOjos colorOjos, ColorPelo colorPelo,
			boolean calvicie, boolean lentes, CasaHogwarts casa, SangreLimpia sangreLimpia, boolean alumno) {
		this.id = contador++;
		this.nombre = nombre;
		this.genero = genero;
		this.edad = edad;
		this.colorOjos = colorOjos;
		this.colorPelo = colorPelo;
		this.calvicie = calvicie;
		this.lentes = lentes;
		this.casa = casa;
		this.sangreLimpia = sangreLimpia;
		this.alumno = alumno;
	}

	
	// Métodos
	public boolean cumpleFiltro(TipoFiltro tipo, String valorEsperado) {
		switch (tipo) {
	        case GENERO:
	            return genero.name().equalsIgnoreCase(valorEsperado);
	        case EDAD:
	        	return edad.name().equalsIgnoreCase(valorEsperado);
	        case COLOR_OJOS:
	            return colorOjos.name().equalsIgnoreCase(valorEsperado);
	        case COLOR_PELO:
	            return colorPelo.name().equalsIgnoreCase(valorEsperado);
	        case CALVICIE:
	            return calvicie == Boolean.parseBoolean(valorEsperado);
	        case LENTES:
	            return lentes == Boolean.parseBoolean(valorEsperado);
	        case CASA_HOGWARTS:
	        	return casa.name().equalsIgnoreCase(valorEsperado);
	        case SANGRE_LIMPIA:
	        	return sangreLimpia.name().equalsIgnoreCase(valorEsperado);
	        case ALUMNO:
	        	return alumno == Boolean.parseBoolean(valorEsperado);
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
	
	public CasaHogwarts getCasa() {
		return casa;
	}

	public SangreLimpia getSangreLimpia() {
		return sangreLimpia;
	}

	public boolean esAlumno() {
		return alumno;
	}
}
