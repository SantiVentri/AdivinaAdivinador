package players;

import model.FiltroAplicado;

public class Consulta {
	private String nombreJugador;
	private FiltroAplicado filtro;
	private boolean respuesta;

	public Consulta(String nombreJugador, FiltroAplicado filtro, boolean respuesta) {
		this.nombreJugador = nombreJugador;
		this.filtro = filtro;
		this.respuesta = respuesta;
	}

	// Getters
	public String getNombreJugador() {
		return nombreJugador;
	}

	public FiltroAplicado getFiltro() {
		return filtro;
	}

	public String getPregunta() {
		return filtro.clave();
	}

	public boolean getRespuesta() {
		return respuesta;
	}
}
