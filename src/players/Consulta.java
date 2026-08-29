package players;

public class Consulta {
	private String nombreJugador;
	private String pregunta;
	private boolean respuesta;
	
	public Consulta(String nombreJugador, String pregunta, boolean respuesta) {
		this.nombreJugador = nombreJugador;
		this.pregunta = pregunta;
		this.respuesta = respuesta;
	}
	
	// Getters
	public String getNombreJugador() {
		return nombreJugador;
	}

	public String getPregunta() {
		return pregunta;
	}

	public boolean getRespuesta() {
		return respuesta;
	}
}
